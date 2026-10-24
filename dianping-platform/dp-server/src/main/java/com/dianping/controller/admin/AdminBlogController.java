package com.dianping.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.service.BlogService;
import com.dianping.vo.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员端帖子管理（内容管理 + 审核中心 bizType=7）
 */
@RestController
@RequestMapping("/api/admin/blog")
public class AdminBlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 帖子列表
     *
     * @param status 0 正常 1 隐藏 2 删除（不传查全部）
     */
    @GetMapping("/list")
    public Result<Page<BlogVO>> list(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) String keyword) {
        return Result.success(blogService.adminList(status, keyword, current, size));
    }

    /**
     * 帖子状态：0 正常 / 1 隐藏 / 2 删除
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        blogService.adminUpdateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除帖子（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        blogService.adminUpdateStatus(id, 2);
        return Result.success();
    }

    /**
     * 删除帖子评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        blogService.adminDeleteComment(id);
        return Result.success();
    }
}