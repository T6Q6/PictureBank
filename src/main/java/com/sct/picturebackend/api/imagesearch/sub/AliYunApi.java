package com.sct.picturebackend.api.imagesearch.sub;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sct.picturebackend.api.imagesearch.model.CreateOutPaintingTaskRequest;
import com.sct.picturebackend.api.imagesearch.model.CreateOutPaintingTaskResponse;
import com.sct.picturebackend.api.imagesearch.model.GetOutPaintingTaskResponse;
import com.sct.picturebackend.exception.BusinessException;
import com.sct.picturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;


    //创建任务地址
    public static final String CREATE_OUT_PAINING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    //查询任务地址
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    //curl --location --request POST 'https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting' \
    //--header "Authorization: Bearer $DASHSCOPE_API_KEY" \
    //--header 'X-DashScope-Async: enable' \
    //--header 'Content-Type: application/json' \
    //--data '{
    //    "model": "image-out-painting",
    //    "input": {
    //        "image_url": "http://xxx/image.jpg"
    //    },
    //    "parameters":{
    //        "angle": 45,
    //        "x_scale":1.5,
    //        "y_scale":1.5
    //    }
    //}'

    /**
     * 创建任务
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        //发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                //开启异步请求
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("创建任务失败：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败");
            }
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI扩图失败，errorCode:{}，errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图失败：" + errorMessage);
            }
            return response;
        }
    }

    //curl -X GET https://dashscope.aliyuncs.com/api/v1/tasks/86ecf553-d340-4e21-xxxxxxxxx \
    //--header "Authorization: Bearer $DASHSCOPE_API_KEY"

    /**
     * 查询创建的任务
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务ID为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("查询任务失败：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "查询任务失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
