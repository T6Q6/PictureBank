package com.sct.picturebackend.api.imagesearch;

import com.sct.picturebackend.api.imagesearch.model.ImageSearchResult;
import com.sct.picturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.sct.picturebackend.api.imagesearch.sub.GetImageListApi;
import com.sct.picturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 门面模式
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        // 获取图搜图的网址
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        // 获取图片列表页面地址
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        // 获取图片列表
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = searchImage("https://pic.nximg.cn/file/20220825/33331825_003626027127_2.jpg");
        System.out.println("搜索成功，结果列表：" + imageList);
    }
}
