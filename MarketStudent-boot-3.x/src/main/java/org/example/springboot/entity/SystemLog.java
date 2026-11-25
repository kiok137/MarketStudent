package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_log")
@Schema(description = "系统日志实体")
public class SystemLog {
    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Integer logId;
    
    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;
    
    @Schema(description = "操作类型: 0-登录, 1-登出, 2-增加, 3-删除, 4-修改, 5-查询, 6-其他")
    @TableField("operation_type")
    private Integer operationType;
    
    @Schema(description = "操作模块")
    @TableField("operation_module")
    private String operationModule;
    
    @Schema(description = "操作描述")
    @TableField("operation_desc")
    private String operationDesc;
    
    @Schema(description = "IP地址")
    @TableField("ip_address")
    private String ipAddress;
    
    @Schema(description = "操作时间")
    @TableField("operation_time")
    private LocalDateTime operationTime;
    
    @Schema(description = "设备信息")
    @TableField("device_info")
    private String deviceInfo;
    
    @Schema(description = "操作状态: 0-失败, 1-成功")
    private Integer status;
    
    @Schema(description = "请求URL")
    @TableField("request_url")
    private String requestUrl;
    
    @Schema(description = "请求方法")
    @TableField("request_method")
    private String requestMethod;
    
    @Schema(description = "请求参数")
    @TableField("request_params")
    private String requestParams;
    
    @Schema(description = "执行时长(毫秒)")
    @TableField("execution_time")
    private Long executionTime;
    
    @Schema(description = "异常信息")
    @TableField("exception_info")
    private String exceptionInfo;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username;
} 