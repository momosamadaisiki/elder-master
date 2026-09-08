package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 床位
 */
@Data
@TableName("bed")
public class Bed {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long roomId;

    /** 床号（如 101-1） */
    private String bedNo;

    /** 状态：0空闲 1占用 2维修 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 非表字段：房间展示信息 */
    @TableField(exist = false)
    private String roomCode;

    @TableField(exist = false)
    private Integer floor;
}
