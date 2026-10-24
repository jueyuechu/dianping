package com.dianping.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.result.Result;
import com.dianping.dto.BlogCommentDTO;
import com.dianping.dto.BlogDTO;
import com.dianping.entity.BlogComment;
import com.dianping.service.BlogService;
import com.dianping.vo.BlogCommentVO;
import com.dianping.vo.BlogVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端社区发帖接口（浏览类 GET 可匿名，写操作需登录）
 */
@RestController
@RequestMapping("/api/user/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 笔记流：type=recommend/latest/follow，keyword 为搜索关键词
     */
    @GetMapping("/feed")
    public Result<Page<BlogVO>> feed(@RequestParam(defaultValue = "recommend") String type,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size) {
        return Result.success(blogService.feed(type, keyword, current, size));
    }

    /**
     * 我的帖子
     */
    @GetMapping("/my")
    public Result<Page<BlogVO>> my(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size) {
        return Result.success(blogService.my(current, size));
    }

    /**
     * 店铺探店笔记
     */
    @GetMapping("/shop/{shopId}")
    public Result<Page<BlogVO>> byShop(@PathVariable Long shopId,
                                       @RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size) {
        return Result.success(blogService.byShop(shopId, current, size));
    }

    /**
     * 帖子详情
     */
    @GetMapping("/{id}")
    public Result<BlogVO> detail(@PathVariable Long id) {
        return Result.success(blogService.detail(id));
    }

    /**
     * 发布帖子（先发后审）
     */
    @PostMapping
    public Result<BlogVO> create(@RequestBody @Valid BlogDTO dto) {
        return Result.success(blogService.create(dto));
    }

    /**
     * 编辑帖子
     */
    @PutMapping("/{id}")
    public Result<BlogVO> update(@PathVariable Long id, @RequestBody @Valid BlogDTO dto) {
        return Result.success(blogService.update(id, dto));
    }

    /**
     * 删除帖子（作者本人）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        blogService.delete(id);
        return Result.success();
    }

    /**
     * 点赞 / 取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        boolean liked = blogService.toggleLike(id);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        return Result.success(data);
    }

    /**
     * 评论列表
     */
    @GetMapping("/{id}/comments")
    public Result<Page<BlogCommentVO>> comments(@PathVariable Long id,
                                               @RequestParam(defaultValue = "1") long current,
                                               @RequestParam(defaultValue = "20") long size) {
        return Result.success(blogService.comments(id, current, size));
    }

    /**
     * 发表评论（楼中楼）
     */
    @PostMapping("/{id}/comment")
    public Result<BlogComment> comment(@PathVariable Long id, @RequestBody @Valid BlogCommentDTO dto) {
        return Result.success(blogService.comment(id, dto));
    }

    /**
     * 删除评论（作者本人）
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        blogService.deleteComment(id);
        return Result.success();
    }

    /**
     * 举报帖子
     */
    @PostMapping("/{id}/report")
    public Result<Void> report(@PathVariable Long id, @RequestParam String reason) {
        blogService.report(id, reason);
        return Result.success();
    }
}