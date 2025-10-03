package com.sct.picturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.sct.picturebackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.Alias;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CosManager {

    //引入对象存储配置
    @Resource
    private CosClientConfig cosClientConfig;

    //引入对象存储客户端
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    public PutObjectResult putPictureObject(String key, File file) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
            //对图片进行处理（获取基本信息）
            PicOperations picOperations = new PicOperations();
            //1 表示返回原图信息
            picOperations.setIsPicInfo(1);
            List<PicOperations.Rule> rules = new ArrayList<>();
            //图片压缩（转成 webp 格式）
            String webpKey = FileUtil.mainName(key) + ".webp";
            PicOperations.Rule compressRule = new PicOperations.Rule();
            compressRule.setRule("imageMogr2/format/webp");
            compressRule.setBucket(cosClientConfig.getBucket());
            compressRule.setFileId(webpKey);
            rules.add(compressRule);
            //缩略图处理，仅对大于2M的图片生效
            if (file.length() > 2 * 1024) {
                PicOperations.Rule thumbnailRule = new PicOperations.Rule();
                thumbnailRule.setBucket(cosClientConfig.getBucket());
                String thumbnaiKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
                thumbnailRule.setFileId(thumbnaiKey);
                //缩放规则 thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
                thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
                rules.add(thumbnailRule);
            }
            //构造处理参数
            picOperations.setRules(rules);
            putObjectRequest.setPicOperations(picOperations);
            return cosClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("上传文件到COS失败，key={}, file={}", key, file.getAbsolutePath(), e);
            throw e;
        }
    }

    /**
     * 删除单一对象
     */
    public void deleteObject(String key) {
        try {
            cosClient.deleteObject(cosClientConfig.getBucket(), key);
            log.info("成功删除COS对象: {}", key);
        } catch (Exception e) {
            log.error("删除COS对象失败，key={}", key, e);
            throw e;
        }
    }

    /**
     * 批量删除对象
     */
    public void deleteObjects(List<String> keys) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
        for (String key : keys) {
            keyVersions.add(new DeleteObjectsRequest.KeyVersion(key));
        }
        deleteObjectsRequest.setKeys(keyVersions);
        cosClient.deleteObjects(deleteObjectsRequest);
    }
}
