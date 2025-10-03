package com.sct.picturebackend.service;

import com.sct.picturebackend.model.dto.space.SpaceAddRequest;
import com.sct.picturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sct.picturebackend.model.entity.User;

/**
 * @author huawei
 * @description 针对表【space(空间表)】的数据库操作Service
 * @createDate 2025-09-30 17:18:36
 */
public interface SpaceService extends IService<Space> {
    /**
     * 校验空间数据
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别自动填充限额数据
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
