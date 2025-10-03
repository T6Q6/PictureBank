package com.sct.picturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.sct.picturebackend.config.CosClientConfig;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.manager.CosManager;
import com.sct.picturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosManager cosManager;
    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 模板方法，定义上传流程
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        String contentType = validPicture(inputSource);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginFilename(inputSource);
        String uploadFilename;
        if (contentType != null && !contentType.isEmpty()) {
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, contentType);
        } else {
            String suffix = FileUtil.getSuffix(originalFilename);
            // 如果原始文件名没有后缀，则使用默认后缀
            if (suffix == null || suffix.isEmpty()) {
                suffix = "jpg"; // 默认后缀
            }
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        }
        log.info("上传图片，uploadFileName:{}", uploadFilename);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
            //处理文件来源（本地或URL）
            processFile(inputSource, file);
            //上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (!CollUtil.isEmpty(objectList)) {
                CIObject compressCiObject = objectList.get(0);
                //缩略图默认等于压缩图
                CIObject thumbnailCiObject = compressCiObject;
                //有生成缩略图才能得到缩略图
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                //分装压缩图返回结果
                return buildResult(originalFilename, compressCiObject, thumbnailCiObject, imageInfo);
            }
            //分装原图返回
            return buildResult(uploadPath, file, originalFilename, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
    }


    /**
     * 封装返回结果
     *
     * @param uploadPath
     * @param file
     * @param originalFilename
     * @return
     */
    private UploadPictureResult buildResult(String uploadPath, File file, String originalFilename, ImageInfo imageInfo) {
        try {
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicColor(imageInfo.getAve());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("上传图片到COS失败，uploadPath={}, originalFilename={}", uploadPath, originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片到存储服务失败: " + e.getMessage());
        }
    }

    /**
     * 校验输入源并生成本地临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 校验输入源
     */
    protected abstract String validPicture(Object inputSource);

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        //删除
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 封装返回结果（压缩后）
     *
     * @param originalFilename
     * @param compressCiObject
     * @param thumbnailCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        try {
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = compressCiObject.getWidth();
            int picHeight = compressCiObject.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(compressCiObject.getFormat());
            uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
            //设置图片为压缩后的地址
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
            uploadPictureResult.setPicColor(imageInfo.getAve());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("上传图片到COS失败，uploadPath={}, originalFilename={}", compressCiObject.getKey(), originalFilename, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片到存储服务失败: " + e.getMessage());
        }
    }
}
