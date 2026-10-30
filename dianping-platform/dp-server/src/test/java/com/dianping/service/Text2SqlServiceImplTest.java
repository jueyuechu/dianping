package com.dianping.service;

import com.dianping.common.exception.BaseException;
import com.dianping.common.properties.AiProperties;
import com.dianping.service.impl.Text2SqlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class Text2SqlServiceImplTest {

    private Text2SqlServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new Text2SqlServiceImpl();
        AiProperties properties = new AiProperties();
        properties.setMaxRows(50);
        ReflectionTestUtils.setField(service, "aiProperties", properties);
    }

    @Test
    void selectShouldBeAcceptedAndLimited() {
        String sql = ReflectionTestUtils.invokeMethod(service, "validateAndNormalize",
                "SELECT COUNT(*) FROM tb_user");
        assertNotNull(sql);
        assertTrue(sql.endsWith("LIMIT 50"));
    }

    @Test
    void writeSqlShouldBeRejected() {
        assertThrows(BaseException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "validateAndNormalize", "DELETE FROM tb_user"));
        assertThrows(BaseException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "validateAndNormalize", "UPDATE tb_user SET status = 0"));
    }

    @Test
    void tableOutsideWhitelistShouldBeRejected() {
        assertThrows(BaseException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "validateAndNormalize", "SELECT * FROM tb_admin"));
    }

    @Test
    void dangerousSubstringShouldBeRejected() {
        assertThrows(BaseException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "validateAndNormalize", "SELECT * FROM tb_user WHERE sleep(1) = 0"));
    }
}
