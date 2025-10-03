package com.sct.picturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sct.picturebackend.annotation.AuthCheck;
import com.sct.picturebackend.constant.UserConstant;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import com.sct.picturebackend.mapper.UserMapper;
import com.sct.picturebackend.model.dto.user.UserAddRequest;
import com.sct.picturebackend.model.dto.user.UserQueryRequest;
import com.sct.picturebackend.model.enums.UserRoleEnum;
import com.sct.picturebackend.model.vo.LoginUserVO;
import com.sct.picturebackend.model.vo.UserVO;
import com.sct.picturebackend.service.UserService;
import com.sct.picturebackend.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.sct.picturebackend.constant.UserConstant.USER_LOGIN_STATE;
import static com.sct.picturebackend.constant.UserConstant.USER_ROLE;

/**
 * @author huawei
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-09-24 11:58:22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空"));
        ThrowUtils.throwIf(userAccount.length() < 4,
                new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短"));
        ThrowUtils.throwIf((userPassword.length() < 8 || checkPassword.length() < 8),
                new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短"));
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致"));
        //2. 检查是否重复注册
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount));
        ThrowUtils.throwIf(count > 0,
                new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复"));
        //3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("默认名称");
        user.setUserRole(USER_ROLE);
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult,
                new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误"));
        return user.getId();
    }

    /**
     * 加密
     *
     * @param password
     * @return
     */
    @Override
    public String getEncryptPassword(String password) {
        //盐值，混淆密码
        final String SALT = "TUSHUGUANLIsct";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空"));
        ThrowUtils.throwIf(userAccount.length() < 4,
                new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误"));
        ThrowUtils.throwIf(userPassword.length() < 8,
                new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误"));
        //2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户是否存在
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, userPassword));
        //用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 得到LoginUserVO响返回结果
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //判断是否已登录
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) user;
        ThrowUtils.throwIf(currentUser == null,
                new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
        return currentUser;
    }

    /**
     * 用户退出
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //1. 判断是否已经登录
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(user == null,
                new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
        //2. 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取单个用户脱敏信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 将查询请求转为 LambdaQueryWrapper 对象
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null,
                new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空"));
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 判断是否为管理员
     *
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




