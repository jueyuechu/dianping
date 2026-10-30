package com.dianping.service;

/**
 * 敏感词过滤：高危词拦截，低/中危词打码
 */
public interface SensitiveWordService {

    /**
     * 过滤文本，高危词抛业务异常
     */
    String filter(String text);

    /**
     * 重新加载词库（后台维护敏感词后调用）
     */
    void refresh();
}
