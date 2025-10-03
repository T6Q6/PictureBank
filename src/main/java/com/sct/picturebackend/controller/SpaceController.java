package com.sct.picturebackend.controller;

import com.sct.picturebackend.annotation.AuthCheck;
import com.sct.picturebackend.common.BaseResponse;
import com.sct.picturebackend.common.ResultUtils;
import com.sct.picturebackend.constant.UserConstant;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import com.sct.picturebackend.model.dto.space.SpaceUpdateRequest;
import com.sct.picturebackend.model.entity.Space;
import com.sct.picturebackend.model.enums.SpaceLevelEnum;
import com.sct.picturebackend.model.vo.SpaceLevel;
import com.sct.picturebackend.service.SpaceService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        //自动填充限额数据
        spaceService.fillSpaceBySpaceLevel(space);
        //校验数据
        spaceService.validSpace(space, false);
        //判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, new BusinessException(ErrorCode.OPERATION_ERROR));
        return ResultUtils.success(true);
    }

    @GetMapping("list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        //获取所有枚举类
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}
