package com.sct.picturebackend.mapper;

import com.sct.picturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author huawei
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-09-24 11:58:22
 * @Entity com.sct.picturebackend.model.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




