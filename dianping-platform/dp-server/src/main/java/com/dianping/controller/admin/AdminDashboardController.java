package com.dianping.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.result.Result;
import com.dianping.entity.*;
import com.dianping.mapper.*;
import com.dianping.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端数据看板（骨架阶段：基础统计）
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private GroupbuyOrderMapper groupbuyOrderMapper;

    @Autowired
    private AuditTaskMapper auditTaskMapper;

    @Autowired
    private ReportMapper reportMapper;

    @GetMapping("/overview")
    public Result<DashboardVO> overview() {
        return Result.success(DashboardVO.builder()
                .userCount(userMapper.selectCount(null))
                .merchantCount(merchantMapper.selectCount(null))
                .shopCount(shopMapper.selectCount(null))
                .reviewCount(reviewMapper.selectCount(null))
                .orderCount(groupbuyOrderMapper.selectCount(null))
                .pendingAuditCount(auditTaskMapper.selectCount(
                        new LambdaQueryWrapper<AuditTask>().in(AuditTask::getStatus, 1, 2)))
                .pendingReportCount(reportMapper.selectCount(
                        new LambdaQueryWrapper<Report>().isNull(Report::getResult)))
                .build());
    }
}