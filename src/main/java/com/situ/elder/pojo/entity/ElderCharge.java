package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 老人收费项目
 */
@Data
@TableName("elder_charge")
public class ElderCharge {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private String itemName;

    private BigDecimal amount;

    /** 0一次性 1月度(每月重复) */
    private Integer chargeType;

    /** yyyy-MM */
    private String chargeMonth;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
