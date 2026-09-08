package com.situ.elder.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 探视/访客记录 VO（带老人姓名）
 */
@Data
public class VisitVO {

    private Long id;

    /** 被访老人ID */
    private Long elderId;

    /** 被访老人姓名 */
    private String elderName;

    private String visitorName;
    private String visitorPhone;
    private String relation;
    private String idCardNo;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date visitDate;

    private String startTime;
    private String endTime;
    private String purpose;

    /** 申请方式：0线上申请 1现场登记 */
    private Integer applyWay;

    /** 状态：0待审批 1已通过/在访 2已拒绝 3已完成 4已取消 5已过期 */
    private Integer status;

    private Long approveUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date approveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveTime;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
