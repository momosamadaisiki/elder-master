package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 探视/访客登记
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("visit_record")
public class VisitRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 被访老人ID */
    private Long elderId;

    /** 访客姓名 */
    private String visitorName;

    /** 访客手机号 */
    private String visitorPhone;

    /** 关系：家属/朋友/维修/其他 */
    private String relation;

    /** 身份证号(选填) */
    private String idCardNo;

    /** 探视日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date visitDate;

    /** 预约开始时间 HH:mm */
    private String startTime;

    /** 预约结束时间 HH:mm */
    private String endTime;

    /** 探视事由 */
    private String purpose;

    /** 申请方式：0线上申请 1现场登记 */
    private Integer applyWay;

    /** 状态：0待审批 1已通过/在访 2已拒绝 3已完成(已离院) 4已取消 5已过期 */
    private Integer status;

    /** 审批人ID */
    private Long approveUserId;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approveTime;

    /** 实际到达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arriveTime;

    /** 离院时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveTime;

    /** 备注/审批意见 */
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
