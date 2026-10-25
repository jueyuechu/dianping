package com.dianping.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Text2SQL 结果
 */
@Data
public class Text2SqlResult {

    /** 生成并实际执行的 SQL */
    private String sql;

    /** 列名 */
    private List<String> columns;

    /** 行数据 */
    private List<Map<String, Object>> rows;

    /** 行数 */
    private Integer rowCount;

    /** 自然语言答复 */
    private String answer;

    /** 是否使用本地 mock 规则生成 */
    private Boolean mock;
}