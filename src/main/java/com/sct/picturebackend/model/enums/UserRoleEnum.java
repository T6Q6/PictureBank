package com.sct.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin"),
    VIP("VIP", "vip");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }


//    //用Map缓存所有枚举值来加速查找
//    USER("用户","user"),
//    ADMIN("管理员","admin"),
//    VIP("VIP","vip");
//
//    private final String text;
//
//    private final String value;
//
//    //缓存：用于快速查找（key可以是text或value，根据需求选择）
//    private static final Map<String,UserRoleEnum> TEXT_TO_ENUM=new HashMap<>();
//    private static final Map<String,UserRoleEnum> VALUE_TO_ENUM=new HashMap<>();
//    分别put
//    UserRoleEnum(String text,String value){
//        this.text=text;
//        this.value=value;
//    }
//
//    //快速查找方法（根据value）
//    public static UserRoleEnum getByValue(String value){
//        return VALUE_TO_ENUM.get(value);
//    }
//
//    //快速查找方法（根据key）
//    public static UserRoleEnum getByText(String text){
//        return TEXT_TO_ENUM.get(text);
//    }
}
