package com.sct.picturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import com.sct.picturebackend.model.dto.space.SpaceAddRequest;
import com.sct.picturebackend.model.entity.Space;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.enums.SpaceLevelEnum;
import com.sct.picturebackend.service.SpaceService;
import com.sct.picturebackend.mapper.SpaceMapper;
import com.sct.picturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huawei
 * @description 针对表【space(空间表)】的数据库操作Service实现
 * @createDate 2025-09-30 17:18:36
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 空间数据校验
     *
     * @param space
     * @param add
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //要创建
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName),
                    new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空"));
            ThrowUtils.throwIf(spaceLevel == null,
                    new BusinessException(ErrorCode.PARAMS_ERROR, "空间等级不能为空"));
        }
        //修改时
        if (spaceLevel != null) {
            ThrowUtils.throwIf(spaceLevelEnum == null,
                    new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在"));
        }
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtils.throwIf(spaceName.length() > 30,
                    new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长"));
        }
    }

    /**
     * 根据空间级别自动填充限额数据
     *
     * @param space
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //将DTO转换为实体类
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        //默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //填充数据
        fillSpaceBySpaceLevel(space);
        //校验数据
        validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        //权限校验
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }
        //争对用户进行加锁
        //通过字符串常量池实现锁的唯一性，因为由JVM管理，不会得到即使的释放，所以这样会占用内存
//        String lock = String.valueOf(userId).intern();
//        synchronized (lock){
//            Long newSpaceId = transactionTemplate.execute(status -> {
//                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
//                ThrowUtils.throwIf(exists,
//                        new BusinessException(ErrorCode.OPERATION_ERROR, "用户已存在空间"));
//                //写入数据库
//                boolean result = this.save(space);
//                ThrowUtils.throwIf(!result, new BusinessException(ErrorCode.OPERATION_ERROR));
//                //返回新写入的空间id
//                return space.getId();
//            });
//            return Optional.ofNullable(newSpaceId).orElse(-1L);
//        }
        //通过ConcurrentHashMap来存储锁对象，使其能及时释放
        ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();
        Object lock = lockMap.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            try {
                Long newSpaceId = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                    ThrowUtils.throwIf(exists,
                            new BusinessException(ErrorCode.OPERATION_ERROR, "用户已存在空间"));
                    //写入数据库
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, new BusinessException(ErrorCode.OPERATION_ERROR));
                    //返回新写入的空间id
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                //防止内存泄漏
                lockMap.remove(userId);
            }
        }
    }

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        ThrowUtils.throwIf(space == null,
                new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        ThrowUtils.throwIf(loginUser == null,
                new BusinessException(ErrorCode.NOT_LOGIN_ERROR));
        //判断空间权限,仅允许管理员和空间创建者访问
        if (!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}




