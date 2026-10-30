package com.dianping.service.impl;

import com.dianping.common.exception.BaseException;
import com.dianping.entity.SensitiveWord;
import com.dianping.mapper.SensitiveWordMapper;
import com.dianping.service.SensitiveWordService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    private volatile List<SensitiveWord> words = List.of();

    @PostConstruct
    @Override
    public void refresh() {
        try {
            words = sensitiveWordMapper.selectList(null).stream()
                    .sorted(Comparator.comparingInt((SensitiveWord w) ->
                            w.getWord() == null ? 0 : w.getWord().length()).reversed())
                    .toList();
            log.info("敏感词库已加载 {} 条", words.size());
        } catch (Exception e) {
            log.warn("敏感词库加载失败：{}", e.getMessage());
        }
    }

    @Override
    public String filter(String text) {
        if (text == null || text.isBlank() || words.isEmpty()) {
            return text;
        }
        String result = text;
        for (SensitiveWord item : words) {
            String word = item.getWord();
            if (word == null || word.isBlank() || !result.contains(word)) {
                continue;
            }
            if (item.getLevel() != null && item.getLevel() >= 3) {
                throw new BaseException("内容包含违规词：" + word);
            }
            result = result.replace(word, "*".repeat(word.length()));
        }
        return result;
    }
}
