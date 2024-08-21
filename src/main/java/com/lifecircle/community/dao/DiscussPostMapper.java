package com.lifecircle.community.dao;

import com.lifecircle.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 该方法是查询帖子，当传入userId时，则查询某个人的帖子，当不传入时查询的是所有帖子。需要动态拼接sql语句
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 该方法是查询帖子总数量，@Param注解用来给参数起别名，如果需要动态的拼接条件(比如再<if>里使用)，并且这个方法只有一个参数，那必须起别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

}
