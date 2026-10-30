package com.dianping.service;

import com.dianping.common.exception.BaseException;
import com.dianping.entity.SensitiveWord;
import com.dianping.mapper.SensitiveWordMapper;
import com.dianping.service.impl.SensitiveWordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

class SensitiveWordServiceImplTest {

    private SensitiveWordServiceImpl service;

    @BeforeEach
    void setUp() {
        SensitiveWordMapper mapper = Mockito.mock(SensitiveWordMapper.class);
        when(mapper.selectList(isNull())).thenReturn(List.of(
                word("赌博", 3),
                word("垃圾店", 2),
                word("傻子", 1)));
        service = new SensitiveWordServiceImpl();
        ReflectionTestUtils.setField(service, "sensitiveWordMapper", mapper);
        service.refresh();
    }

    @Test
    void lowRiskWordShouldBeMasked() {
        assertEquals("这家***服务太差了", service.filter("这家垃圾店服务太差了"));
        assertEquals("**才来", service.filter("傻子才来"));
    }

    @Test
    void highRiskWordShouldBeRejected() {
        assertThrows(BaseException.class, () -> service.filter("我想赌博"));
    }

    @Test
    void cleanTextShouldBeUnchanged() {
        assertEquals("火锅很好吃", service.filter("火锅很好吃"));
        assertEquals(null, service.filter(null));
    }

    private SensitiveWord word(String text, int level) {
        SensitiveWord w = new SensitiveWord();
        w.setWord(text);
        w.setLevel(level);
        return w;
    }
}
