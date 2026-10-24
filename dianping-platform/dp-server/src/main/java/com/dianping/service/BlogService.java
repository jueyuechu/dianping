package com.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.dto.BlogCommentDTO;
import com.dianping.dto.BlogDTO;
import com.dianping.entity.BlogComment;
import com.dianping.vo.BlogCommentVO;
import com.dianping.vo.BlogVO;

public interface BlogService {

    /** 发布帖子（探店笔记 / 自由动态），先发后审 */
    BlogVO create(BlogDTO dto);

    /** 编辑帖子 */
    BlogVO update(Long id, BlogDTO dto);

    /** 删除帖子（仅作者） */
    void delete(Long id);

    /** 帖子详情（公开浏览） */
    BlogVO detail(Long id);

    /**
     * 笔记流
     *
     * @param type    recommend 推荐 / latest 最新 / follow 关注
     * @param keyword 搜索关键词（匹配标题/正文，可为空）
     */
    Page<BlogVO> feed(String type, String keyword, long current, long size);

    /** 我的帖子 */
    Page<BlogVO> my(long current, long size);

    /** 店铺探店笔记 */
    Page<BlogVO> byShop(Long shopId, long current, long size);

    /** 点赞 / 取消点赞，返回最新状态 */
    boolean toggleLike(Long blogId);

    /** 评论列表 */
    Page<BlogCommentVO> comments(Long blogId, long current, long size);

    /** 发表评论（楼中楼） */
    BlogComment comment(Long blogId, BlogCommentDTO dto);

    /** 删除评论（仅作者） */
    void deleteComment(Long commentId);

    /** 举报帖子 */
    void report(Long blogId, String reason);

    // ---------------- 管理端 ----------------

    /** 帖子列表（可按状态/关键词筛选） */
    Page<BlogVO> adminList(Integer status, String keyword, long current, long size);

    /** 修改帖子状态：0 正常 1 隐藏 2 删除 */
    void adminUpdateStatus(Long id, Integer status);

    /** 管理端删除评论 */
    void adminDeleteComment(Long commentId);
}