package com.lifecircle.community.controller;

import com.lifecircle.community.entity.Comment;
import com.lifecircle.community.entity.DiscussPost;
import com.lifecircle.community.entity.Page;
import com.lifecircle.community.entity.User;
import com.lifecircle.community.service.CommentService;
import com.lifecircle.community.service.DiscussPostService;
import com.lifecircle.community.service.UserService;
import com.lifecircle.community.util.CommunityConstant;
import com.lifecircle.community.util.CommunityUtil;
import com.lifecircle.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        // 帖子内容
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        // 帖子作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 帖子评论分页设置
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        /**
         * 评论：给帖子的评论
         * 回复：给帖子评论的评论
         * 一条帖子包含多条评论，而一条评论又包含有评论的作者以及回复，回复又包含了回复的作者，这些信息都要展示
         */

        // 查询当前帖子所有的评论，评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment); // 评论
                commentVo.put("user", userService.findUserById(comment.getUserId())); // 评论的作者
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标(targetId)
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());

                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

}
