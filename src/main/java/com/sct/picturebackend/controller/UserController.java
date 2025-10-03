package com.sct.picturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sct.picturebackend.annotation.AuthCheck;
import com.sct.picturebackend.common.BaseResponse;
import com.sct.picturebackend.common.DeleteRequest;
import com.sct.picturebackend.common.ResultUtils;
import com.sct.picturebackend.constant.UserConstant;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import com.sct.picturebackend.model.dto.user.*;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.vo.LoginUserVO;
import com.sct.picturebackend.model.vo.UserVO;
import com.sct.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        log.info("用户注册：{}", userRegisterRequest);
        long result = userService.userRegister(userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword());
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        log.info("用户登录：{}", userLoginRequest);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(),
                userLoginRequest.getUserPassword(), request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        log.info("获取当前用户信息");
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 用户退出
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        log.info("用户退出");
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 添加用户（仅管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        log.info("添加用户:{}", userAddRequest);
        ThrowUtils.throwIf(userAddRequest == null,
                new BusinessException(ErrorCode.PARAMS_ERROR));
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //默认密码为12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据id获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        log.info("根据id获取用户（仅管理员）:{}", id);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        log.info("根据id获取包装类:{}", id);
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除用户:{}", deleteRequest);
        ThrowUtils.throwIf((deleteRequest == null || deleteRequest.getId() <= 0),
                new BusinessException(ErrorCode.PARAMS_ERROR));
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, new BusinessException(ErrorCode.OPERATION_ERROR));
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        log.info("更新用户:{}", userUpdateRequest);
        ThrowUtils.throwIf((userUpdateRequest == null || userUpdateRequest.getId() == null),
                new BusinessException(ErrorCode.PARAMS_ERROR));
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, new BusinessException(ErrorCode.OPERATION_ERROR));
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        log.info("分页获取用户封装列表（仅管理员）:{}", userQueryRequest);
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
