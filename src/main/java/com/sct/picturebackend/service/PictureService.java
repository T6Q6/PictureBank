package com.sct.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sct.picturebackend.api.imagesearch.model.CreateOutPaintingTaskRequest;
import com.sct.picturebackend.api.imagesearch.model.CreateOutPaintingTaskResponse;
import com.sct.picturebackend.common.DeleteRequest;
import com.sct.picturebackend.model.dto.picture.*;
import com.sct.picturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sct.picturebackend.model.entity.Space;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.vo.PictureVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author huawei
 * @description 针对表【picture(图片表)】的数据库操作Service
 * @createDate 2025-09-26 12:41:25
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询包装类
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片校验
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 根据用户角色填充审核字段
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);

    Boolean deletePicture(DeleteRequest deleteRequest, HttpServletRequest request);

    Boolean editPicture(PictureEditRequest pictureEditRequest,
                        HttpServletRequest request);

    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    @Transactional(rollbackFor = Exception.class)
    void editPictureBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

//    @Transactional(rollbackFor = Exception.class)
//    void batchEditPictureMetadata(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    CreateOutPaintingTaskResponse createOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                        User loginUser);

    /**
     * 删除单个图片
     */
    void clearPictureFile(Picture oldPicture);

    void checkPictureAuth(User loginUser, Picture picture);
}
