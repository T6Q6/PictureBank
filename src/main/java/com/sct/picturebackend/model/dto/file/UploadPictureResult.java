package com.sct.picturebackend.model.dto.file;

import lombok.Data;

@Data
public class UploadPictureResult {

    /**
     * 图片 url
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 文件体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 缩略图 url
     */
    private String thumbnailUrl;

    /**
     * 图片主色调
     */
    private String picColor;

}
