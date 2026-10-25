package com.dianping.service.impl;

import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.AiProperties;
import com.dianping.service.Text2SqlService;
import com.dianping.vo.Text2SqlResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text2SQL 数据助手
 * 安全策略：仅允许 SELECT、表白名单、禁止系统库/危险关键字、强制 LIMIT、只读执行
 */
@Slf4j
@Service
public class Text2SqlServiceImpl implements Text2SqlService {

    private static final Set<String> ALLOWED_TABLES = Set.of(
            "tb_user", "tb_merchant", "tb_shop", "tb_dish", "tb_dish_category",
            "tb_groupbuy", "tb_groupbuy_order", "tb_verification_record", "tb_review",
            "tb_blog", "tb_blog_comment", "tb_audit_task", "tb_report", "tb_payment", "tb_category");

    /** 危险操作关键字（按单词边界匹配，避免 deleted/update_time 误伤） */
    private static final List<String> FORBIDDEN_WORDS = List.of(
            "insert", "update", "delete", "drop", "alter", "truncate", "create",
            "grant", "revoke", "replace", "merge", "call", "execute");

    /** 危险子串（系统库、文件导出、延时函数等） */
    private static final List<String> FORBIDDEN_SUBSTR = List.of(
            "information_schema", "mysql.", "performance_schema",
            "outfile", "dumpfile", "sleep(", "benchmark(");

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("\\b(?:from|join)\\s+`?([a-z_][a-z0-9_]*)`?", Pattern.CASE_INSENSITIVE);

    private static final String SCHEMA_PROMPT = """
            你是 MySQL 数据分析助手。根据用户问题生成一条只读 SELECT 语句。
            可用表（表前缀 tb_）：
            - tb_user(id, nick_name, phone, status, create_time)：用户，status=1 正常
            - tb_merchant(id, name, status, create_time)：商户，status=0 待审核/1 正常/2 冻结
            - tb_shop(id, merchant_id, name, type_id, avg_price, sold, comments, score, create_time)：店铺，score 为评分×10
            - tb_dish(id, shop_id, name, price, stock, status, sold)
            - tb_groupbuy(id, shop_id, name, original_price, groupbuy_price, stock, sold, status)
            - tb_groupbuy_order(id, order_no, user_id, groupbuy_id, shop_id, quantity, amount, status, create_time)：status 1待支付 2待使用 3已使用 4已退款 5已过期
            - tb_verification_record(id, order_id, operator_id, verify_time)
            - tb_review(id, user_id, shop_id, score, content, status, create_time)：score 为评分×10
            - tb_blog(id, user_id, shop_id, type, content, status, liked, comments, create_time)
            - tb_audit_task(id, biz_type, biz_id, status, create_time)：status 1待审核 2审核中 3通过 4驳回
            - tb_report(id, reporter_id, target_type, target_id, reason, result, create_time)：result 为空表示待处理
            规则：
            1. 只输出 SQL，不要解释、不要 Markdown 代码块；
            2. 只能 SELECT，禁止任何写操作；
            3. 统计类请使用 COUNT/SUM/AVG 并起英文别名；
            4. 涉及删除标记的表需要 deleted = 0 条件；
            5. 明细查询最多 LIMIT 50。
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AiProperties aiProperties;

    /** mock=false 时才存在 ChatClient（见 AiConfig） */
    @Autowired
    private ObjectProvider<ChatClient> chatClientProvider;

    @Override
    public Text2SqlResult ask(String question) {
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) {
            throw new BaseException("请输入要查询的问题");
        }
        boolean mock = aiProperties.isMock();
        String sql = mock ? mockSql(q) : llmSql(q);
        String safeSql = validateAndNormalize(sql);

        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbcTemplate.query(safeSql, (ResultSetExtractor<Void>) rs -> {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                columns.add(md.getColumnLabel(i));
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columns.size(); i++) {
                    row.put(columns.get(i - 1), rs.getObject(i));
                }
                rows.add(row);
            }
            return null;
        });

        Text2SqlResult result = new Text2SqlResult();
        result.setSql(safeSql);
        result.setColumns(columns);
        result.setRows(rows);
        result.setRowCount(rows.size());
        result.setMock(mock);
        result.setAnswer(buildAnswer(rows.size(), columns));
        return result;
    }

    /**
     * 本地规则（无 API Key 时演示用）
     */
    private String mockSql(String q) {
        if (matches(q, "删除", "清空", "修改", "更新", "插入", "移除", "drop", "truncate", "alter", "写入")) {
            throw new BaseException("AI 助手仅支持数据查询，不支持删除、修改等写操作");
        }
        if (matches(q, "待审核", "审核中", "待处理审核")) {
            return "SELECT COUNT(*) AS pending_audit FROM tb_audit_task WHERE status IN (1,2) AND deleted = 0";
        }
        if (matches(q, "举报")) {
            return "SELECT COUNT(*) AS pending_report FROM tb_report WHERE result IS NULL AND deleted = 0";
        }
        if (matches(q, "销售额", "gmv", "成交额", "交易额", "营收")) {
            return "SELECT COUNT(*) AS order_count, IFNULL(SUM(amount),0) AS gmv FROM tb_groupbuy_order WHERE deleted = 0";
        }
        if (matches(q, "最近的订单", "最新订单", "最近订单", "订单明细", "订单列表")) {
            return "SELECT order_no, amount, status, create_time FROM tb_groupbuy_order WHERE deleted = 0 ORDER BY create_time DESC LIMIT 10";
        }
        if (matches(q, "订单", "交易", "下单")) {
            return "SELECT COUNT(*) AS order_count, IFNULL(SUM(amount),0) AS gmv FROM tb_groupbuy_order WHERE deleted = 0";
        }
        if (matches(q, "核销")) {
            return "SELECT COUNT(*) AS verify_count FROM tb_verification_record WHERE deleted = 0";
        }
        if (matches(q, "评价", "评分", "口碑")) {
            return "SELECT COUNT(*) AS review_count, ROUND(AVG(score)/10,2) AS avg_score FROM tb_review WHERE deleted = 0";
        }
        if (matches(q, "帖子", "笔记", "社区")) {
            return "SELECT COUNT(*) AS blog_count FROM tb_blog WHERE deleted = 0";
        }
        if (matches(q, "店铺", "门店")) {
            return "SELECT COUNT(*) AS shop_count FROM tb_shop WHERE deleted = 0";
        }
        if (matches(q, "商家", "商户")) {
            return "SELECT COUNT(*) AS merchant_count FROM tb_merchant WHERE deleted = 0";
        }
        if (matches(q, "用户", "会员", "注册")) {
            return "SELECT COUNT(*) AS user_count FROM tb_user WHERE deleted = 0";
        }
        throw new BaseException("暂时无法理解这个问题，可以试试：用户数量 / 商家数量 / 订单销售额 / 评价数量 / 待审核任务 / 待处理举报 / 最近订单");
    }

    private boolean matches(String q, String... keywords) {
        String lower = q.toLowerCase(Locale.ROOT);
        for (String k : keywords) {
            if (lower.contains(k.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过 Spring AI ChatClient 生成 SQL
     */
    private String llmSql(String question) {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new BaseException("LLM 模式未启用，请设置 dianping.ai.mock=false 并配置 spring.ai.openai.api-key");
        }
        try {
            String content = chatClient.prompt()
                    .system(SCHEMA_PROMPT)
                    .user(question)
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                throw new BaseException("AI 未生成 SQL");
            }
            return content;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 生成 SQL 失败", e);
            throw new BaseException("AI 服务调用失败，请检查配置");
        }
    }

    /**
     * SQL 安全校验 + 强制 LIMIT
     */
    private String validateAndNormalize(String raw) {
        String sql = raw == null ? "" : raw.trim();
        sql = sql.replaceAll("(?is)^```(?:sql)?", "").replaceAll("```$", "").trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (!sql.toLowerCase(Locale.ROOT).startsWith("select")) {
            throw new BaseException("只允许查询（SELECT）语句");
        }
        String lower = sql.toLowerCase(Locale.ROOT);
        for (String word : FORBIDDEN_WORDS) {
            if (Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find()) {
                throw new BaseException("检测到不允许的 SQL 操作：" + word);
            }
        }
        for (String sub : FORBIDDEN_SUBSTR) {
            if (lower.contains(sub)) {
                throw new BaseException("检测到不允许的 SQL 操作：" + sub);
            }
        }
        Matcher m = TABLE_PATTERN.matcher(sql);
        Set<String> tables = new HashSet<>();
        while (m.find()) {
            tables.add(m.group(1).toLowerCase(Locale.ROOT));
        }
        if (tables.isEmpty()) {
            throw new BaseException("未识别到可查询的数据表");
        }
        for (String t : tables) {
            if (!ALLOWED_TABLES.contains(t)) {
                throw new BaseException("不允许查询该表：" + t);
            }
        }
        if (!lower.contains("limit")) {
            sql = sql + " LIMIT " + Math.max(1, aiProperties.getMaxRows());
        }
        return sql;
    }

    private String buildAnswer(int rowCount, List<String> columns) {
        if (rowCount == 0) {
            return "没有查询到符合条件的数据。";
        }
        if (rowCount == 1 && columns.size() == 1) {
            return "查询结果：" + columns.get(0) + " = " + "（见下表）";
        }
        return "已为你生成只读查询并执行，共返回 " + rowCount + " 行数据。";
    }
}