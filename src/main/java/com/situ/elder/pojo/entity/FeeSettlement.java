package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 退住费用结算单
 */
@Data
@TableName("fee_settlement")
public class FeeSettlement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private Long stayId;

    /** 计费天数 */
    private Integer days;

    /** 护理等级名称（快照） */
    private String careLevelName;

    /** 护理费：等级月费/30×天数 */
    private BigDecimal careAmount;

    /** 床位信息快照（如 101 / 101-1） */
    private String bedInfo;

    /** 床位费：房间月费/30×天数 */
    private BigDecimal bedAmount;

    private BigDecimal totalAmount;

    /** 0待缴费 1已结清 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date settleTime;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
