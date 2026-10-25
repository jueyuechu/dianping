package com.dianping.service;

import com.dianping.vo.Text2SqlResult;

public interface Text2SqlService {

    /**
     * 自然语言转 SQL，只读执行并返回结果
     */
    Text2SqlResult ask(String question);
}