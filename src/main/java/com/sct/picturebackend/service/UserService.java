package com.sct.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sct.picturebackend.model.dto.user.UserAddRequest;
import com.sct.picturebackend.model.dto.user.UserQueryRequest;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.vo.LoginUserVO;
import com.sct.picturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author huawei
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-09-24 11:58:22
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 得到LoginUserVO返回结果
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取单个用户脱敏信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 将查询请求转为 QueryWrapper 对象
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断当前用户是否为管理员
     */
    boolean isAdmin(User user);
}
