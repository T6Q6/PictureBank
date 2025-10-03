package com.sct.picturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = -4724481317353284327L;

    private List<String> tagList;
    private List<String> categoryList;
}
