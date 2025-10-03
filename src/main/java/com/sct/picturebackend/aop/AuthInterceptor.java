package com.sct.picturebackend.aop;

import com.sct.picturebackend.annotation.AuthCheck;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.enums.UserRoleEnum;
import com.sct.picturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限
        if (mustRoleEnum == null) {
            joinPoint.proceed();
        }
        //一下为：必须有该权限才通过
        //获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限，拒绝
        ThrowUtils.throwIf(userRoleEnum == null,
                new BusinessException(ErrorCode.NO_AUTH_ERROR));
        //要求至少有普通用户权限,没有则拒绝
        ThrowUtils.throwIf(UserRoleEnum.USER.equals(mustRoleEnum)
                        && !UserRoleEnum.USER.equals(userRoleEnum)
                        && !UserRoleEnum.ADMIN.equals(userRoleEnum)
                        && !UserRoleEnum.VIP.equals(userRoleEnum),
                new BusinessException(ErrorCode.NO_AUTH_ERROR));
        //要求必须有管理员权限，普通用户或VIP没有管理员权限，拒绝
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustRoleEnum)
                        && !UserRoleEnum.ADMIN.equals(userRoleEnum),
                new BusinessException(ErrorCode.NO_AUTH_ERROR));
        //要求必须有VIP权限，普通用户没有VIP权限，拒绝，但是管理员权限可以
        ThrowUtils.throwIf(UserRoleEnum.VIP.equals(mustRoleEnum)
                        && !UserRoleEnum.VIP.equals(userRoleEnum)
                        && !UserRoleEnum.ADMIN.equals(userRoleEnum),
                new BusinessException(ErrorCode.NO_AUTH_ERROR));
        //通过校验，放行
        return joinPoint.proceed();
    }
}
