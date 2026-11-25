package org.example.springboot.enumClass;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum OperationType {
    LOGIN(0, "登录"),
    LOGOUT(1, "登出"),
    INSERT(2, "新增"),
    DELETE(3, "删除"),
    UPDATE(4, "修改"),
    SELECT(5, "查询"),
    OTHER(6, "其他");
    
    @EnumValue
    private final Integer code;
    private final String desc;
    
    OperationType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
} 