package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 房间
 */
@Data
@TableName("room")
public class Room {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer floor;

    /** 房间号（如 101） */
    private String code;

    /** 床位数 */
    private Integer capacity;

    /** 床位月费（元/月/床） */
    private BigDecimal price;

    /** 状态：0停用 1正常 */
    private Integer status;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 非表字段：占用/总床位（列表展示用） */
    @TableField(exist = false)
    private Integer occupied;

    @TableField(exist = false)
    private Integer bedTotal;
}
