package com.sct.picturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import com.sct.picturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        log.info("上传图片，fileUrl:{}", fileUrl);
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 针对ai扩图进行获取文件名称
        if (fileUrl.contains("result-") && fileUrl.contains("OSSAccessKeyId")) {
            int start = fileUrl.indexOf("result-");
            int end = fileUrl.indexOf("?OSSAccessKeyId");
            return fileUrl.substring(start, end);
        }
        return FileUtil.mainName(fileUrl) + "." + FileUtil.extName(fileUrl);
    }

    @Override
    protected String validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        try {
            //1.校验url格式
            //验证是否是合法的url
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        //2.校验url协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        //3.发送HEAD请求以验证文件是否存在
        HttpResponse response = null;
        response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
        //未正常返回，无需执行其他判断
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            return null;
        }
        //4.校验文件类型
        String contentType = response.header("Content-Type");
        if (StrUtil.isNotBlank(contentType)) {
            //允许的图片类型
            final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
            ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
        String fromContentType = getSuffixFromContentType(contentType);
        //5.校验文件大小
        String contentLengthStr = response.header("Content-Length");
        if (StrUtil.isNotBlank(contentLengthStr)) {
            try {
                long contentLength = Long.parseLong(contentLengthStr);
                final long TWO_MB = 2 * 1024 * 1024L;
                ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件大小格式错误");
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
        return fromContentType;
    }

    private String getSuffixFromContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return "jpg";
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            default:
                return "";
        }
    }
}
