package com.sct.picturebackend.common;

import lombok.Data;

@Data
public class DeleteRequest {

    /**
     * id
     */
    private Long id;
    //    判断 “同一个类” 的不同版本是否兼容（序列化“身份证”）
    private static final long serialVersionUID = 1L;
}
