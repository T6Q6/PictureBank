package com.sct.picturebackend.mapper;

import com.sct.picturebackend.model.entity.Space;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author huawei
 * @description 针对表【space(空间表)】的数据库操作Mapper
 * @createDate 2025-09-30 17:18:36
 * @Entity com.sct.picturebackend.model.entity.Space
 */
@Mapper
public interface SpaceMapper extends BaseMapper<Space> {

}




