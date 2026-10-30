package com.dianping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.common.context.BaseContext;
import com.dianping.common.exception.BaseException;
import com.dianping.dto.BlogCommentDTO;
import com.dianping.dto.BlogDTO;
import com.dianping.entity.*;
import com.dianping.mapper.*;
import com.dianping.service.BlogService;
import com.dianping.service.SensitiveWordService;
import com.dianping.service.UserBanService;
import com.dianping.vo.BlogCommentVO;
import com.dianping.vo.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private BlogCommentMapper blogCommentMapper;

    @Autowired
    private BlogLikeMapper blogLikeMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserBanService userBanService;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlogVO create(BlogDTO dto) {
        Long userId = currentUserId();
        Integer type = dto.getType() == null ? 1 : dto.getType();
        if (type == 1 && dto.getShopId() == null) {
            throw new BaseException("探店笔记需要关联店铺");
        }
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setShopId(dto.getShopId());
        blog.setType(type);
        blog.setTitle(sensitiveWordService.filter(dto.getTitle()));
        blog.setContent(sensitiveWordService.filter(dto.getContent()));
        blog.setImages(join(dto.getImages()));
        blog.setLocation(dto.getLocation());
        // 帖子先发后审：发布即展示，敏感词与举报由审核环节处理
        blog.setStatus(0);
        blog.setLiked(0);
        blog.setComments(0);
        blog.setCollected(0);
        blogMapper.insert(blog);
        return detail(blog.getId());
    }

    @Override
    public BlogVO update(Long id, BlogDTO dto) {
        Blog blog = requireOwnBlog(id);
        blog.setShopId(dto.getShopId());
        blog.setType(dto.getType() == null ? blog.getType() : dto.getType());
        blog.setTitle(sensitiveWordService.filter(dto.getTitle()));
        blog.setContent(sensitiveWordService.filter(dto.getContent()));
        blog.setImages(join(dto.getImages()));
        blog.setLocation(dto.getLocation());
        blogMapper.updateById(blog);
        return detail(id);
    }

    @Override
    public void delete(Long id) {
        Blog blog = requireOwnBlog(id);
        blogMapper.deleteById(blog.getId());
    }

    @Override
    public BlogVO detail(Long id) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        return toVOList(List.of(blog)).get(0);
    }

    @Override
    public Page<BlogVO> feed(String type, String keyword, long current, long size) {
        Page<Blog> page = new Page<>(current, size);
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>().eq(Blog::getStatus, 0);
        // 关键词搜索：标题或正文模糊匹配
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Blog::getContent, keyword).or().like(Blog::getTitle, keyword));
        }
        if ("follow".equalsIgnoreCase(type)) {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return new Page<>(current, size);
            }
            List<Long> followed = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, userId))
                    .stream().map(Follow::getFollowUserId).collect(Collectors.toList());
            if (followed.isEmpty()) {
                return new Page<>(current, size);
            }
            wrapper.in(Blog::getUserId, followed).orderByDesc(Blog::getCreateTime);
        } else if ("recommend".equalsIgnoreCase(type)) {
            // 推荐流：先按热度（点赞+评论）排序，再按时间
            wrapper.orderByDesc(Blog::getLiked).orderByDesc(Blog::getCreateTime);
        } else {
            wrapper.orderByDesc(Blog::getCreateTime);
        }
        Page<Blog> result = blogMapper.selectPage(page, wrapper);
        Page<BlogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    public Page<BlogVO> my(long current, long size) {
        Long userId = currentUserId();
        Page<Blog> result = blogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Blog>()
                        .eq(Blog::getUserId, userId)
                        .orderByDesc(Blog::getCreateTime));
        Page<BlogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    public Page<BlogVO> byShop(Long shopId, long current, long size) {
        Page<Blog> result = blogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Blog>()
                        .eq(Blog::getShopId, shopId)
                        .eq(Blog::getStatus, 0)
                        .orderByDesc(Blog::getCreateTime));
        Page<BlogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long blogId) {
        Long userId = currentUserId();
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        BlogLike exists = blogLikeMapper.selectOne(new LambdaQueryWrapper<BlogLike>()
                .eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId));
        boolean liked;
        if (exists != null) {
            blogLikeMapper.deleteById(exists.getId());
            blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                    .eq(Blog::getId, blogId)
                    .setSql("liked = GREATEST(liked - 1, 0)"));
            liked = false;
        } else {
            BlogLike like = new BlogLike();
            like.setBlogId(blogId);
            like.setUserId(userId);
            blogLikeMapper.insert(like);
            blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                    .eq(Blog::getId, blogId)
                    .setSql("liked = liked + 1"));
            liked = true;
        }
        return liked;
    }

    @Override
    public Page<BlogCommentVO> comments(Long blogId, long current, long size) {
        Page<BlogComment> result = blogCommentMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<BlogComment>()
                        .eq(BlogComment::getBlogId, blogId)
                        .eq(BlogComment::getStatus, 0)
                        .orderByAsc(BlogComment::getCreateTime));
        return mapComments(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlogComment comment(Long blogId, BlogCommentDTO dto) {
        Long userId = currentUserId();
        // 禁评论封禁校验 + 敏感词过滤
        userBanService.assertAllowed(userId, 3);
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        BlogComment comment = new BlogComment();
        comment.setBlogId(blogId);
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setAnswerId(dto.getAnswerId());
        comment.setContent(sensitiveWordService.filter(dto.getContent()));
        comment.setStatus(0);
        blogCommentMapper.insert(comment);

        blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, blogId)
                .setSql("comments = comments + 1"));
        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        BlogComment comment = blogCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BaseException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(currentUserId())) {
            throw new BaseException(403, "只能删除自己的评论");
        }
        blogCommentMapper.deleteById(commentId);
        blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, comment.getBlogId())
                .setSql("comments = GREATEST(comments - 1, 0)"));
    }

    @Override
    public void report(Long blogId, String reason) {
        Long userId = currentUserId();
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(4);
        report.setTargetId(blogId);
        report.setReason(reason);
        reportMapper.insert(report);
    }

    // ---------------- 管理端 ----------------

    @Override
    public Page<BlogVO> adminList(Integer status, String keyword, long current, long size) {
        Page<Blog> result = blogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Blog>()
                        .eq(status != null, Blog::getStatus, status)
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(Blog::getContent, keyword).or().like(Blog::getTitle, keyword))
                        .orderByDesc(Blog::getCreateTime));
        Page<BlogVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        blog.setStatus(status);
        blogMapper.updateById(blog);
    }

    @Override
    public void adminDeleteComment(Long commentId) {
        BlogComment comment = blogCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BaseException(404, "评论不存在");
        }
        blogCommentMapper.deleteById(commentId);
        blogMapper.update(null, new LambdaUpdateWrapper<Blog>()
                .eq(Blog::getId, comment.getBlogId())
                .setSql("comments = GREATEST(comments - 1, 0)"));
    }

    // ---------------- helpers ----------------

    private Long currentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BaseException(401, "未登录或登录已过期");
        }
        return userId;
    }

    private Blog requireOwnBlog(Long id) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new BaseException(404, "帖子不存在");
        }
        if (!blog.getUserId().equals(currentUserId())) {
            throw new BaseException(403, "只能操作自己的帖子");
        }
        return blog;
    }

    private String join(List<String> images) {
        return images == null || images.isEmpty() ? null : String.join(",", images);
    }

    private List<String> split(String images) {
        if (images == null || images.isBlank()) {
            return List.of();
        }
        return Arrays.stream(images.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList());
    }

    private List<BlogVO> toVOList(List<Blog> blogs) {
        if (blogs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = blogs.stream().map(Blog::getUserId).collect(Collectors.toSet());
        Set<Long> shopIds = blogs.stream().map(Blog::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> shopNames = shopIds.isEmpty() ? Map.of()
                : shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));

        Long current = BaseContext.getCurrentId();
        Set<Long> likedIds = new HashSet<>();
        if (current != null) {
            blogLikeMapper.selectList(new LambdaQueryWrapper<BlogLike>()
                            .eq(BlogLike::getUserId, current)
                            .in(BlogLike::getBlogId, blogs.stream().map(Blog::getId).collect(Collectors.toList())))
                    .forEach(l -> likedIds.add(l.getBlogId()));
        }

        return blogs.stream().map(b -> {
            BlogVO vo = new BlogVO();
            vo.setId(b.getId());
            vo.setUserId(b.getUserId());
            User author = users.get(b.getUserId());
            vo.setAuthorName(author == null ? "用户" : author.getNickName());
            vo.setAuthorIcon(author == null ? null : author.getIcon());
            vo.setShopId(b.getShopId());
            vo.setShopName(b.getShopId() == null ? null : shopNames.get(b.getShopId()));
            vo.setType(b.getType());
            vo.setTitle(b.getTitle());
            vo.setContent(b.getContent());
            vo.setImages(split(b.getImages()));
            vo.setLocation(b.getLocation());
            vo.setStatus(b.getStatus());
            vo.setLikedCount(b.getLiked());
            vo.setCommentCount(b.getComments());
            vo.setCollectCount(b.getCollected());
            vo.setLiked(likedIds.contains(b.getId()));
            vo.setCreateTime(b.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private Page<BlogCommentVO> mapComments(Page<BlogComment> result) {
        Page<BlogCommentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<BlogComment> records = result.getRecords();
        if (records.isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }
        Set<Long> userIds = records.stream().map(BlogComment::getUserId).collect(Collectors.toSet());
        records.stream().map(BlogComment::getAnswerId).filter(Objects::nonNull).forEach(userIds::add);
        Map<Long, String> names = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickName() == null ? "用户" : u.getNickName(), (a, b) -> a));

        voPage.setRecords(records.stream().map(c -> {
            BlogCommentVO vo = new BlogCommentVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setUserName(names.getOrDefault(c.getUserId(), "用户"));
            vo.setParentId(c.getParentId());
            vo.setAnswerId(c.getAnswerId());
            vo.setAnswerName(c.getAnswerId() == null ? null : names.get(c.getAnswerId()));
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
}