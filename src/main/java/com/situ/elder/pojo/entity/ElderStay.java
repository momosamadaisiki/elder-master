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
 * 老人入住单（一条入住对应一条记录，退住后保留历史）
 */
@Data
@TableName("elder_stay")
public class ElderStay {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long elderId;

    private Long bedId;

    /** 入住时护理等级（费用结算用） */
    private Long careLevelId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkOutDate;

    private Long operatorId;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    // ===== 展示用（非表字段）=====
    @TableField(exist = false)
    private String elderName;

    @TableField(exist = false)
    private String careLevelName;

    /** 床位展示：101 / 101-1 */
    @TableField(exist = false)
    private String roomCode;

    @TableField(exist = false)
    private String bedNo;

    @TableField(exist = false)
    private BigDecimal settleAmount;

    /** 结算状态：0待缴费 1已结清（空=未退住无结算） */
    @TableField(exist = false)
    private Integer settleStatus;

    @TableField(exist = false)
    private Long settlementId;
}
