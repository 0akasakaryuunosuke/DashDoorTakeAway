package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作user相关数据接口
 */
public interface UserMapper {

    /**
     * 新增
     */
    int insert(User user);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(User user);

    /**
     * 根据ID查询
     */
    User selectById(Integer id);

    /**
     * 查询所有
     */
    List<User> selectAll(User user);

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

  /**
     * 根据 openid 查询用户
     * @param openid 微信用户唯一标识
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User findByOpenid(@Param("openid") String openid);

    /**
     * 插入新用户
     *
     * @param user 用户信息
     */
    @Insert("INSERT INTO user (username, password, name, avatar, role, sex, phone, openid) " +
            "VALUES (#{username}, #{password}, #{name}, #{avatar}, #{role}, #{sex}, #{phone}, #{openid})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);
}
