package com.situ.elder.pojo.query;

import lombok.Data;

/**
 * 探视/访客查询条件
 */
@Data
public class VisitQuery {

    private Integer page = 1;
    private Integer limit = 10;

    /** 访客姓名 */
    private String visitorName;

    /** 被访老人ID */
    private Long elderId;

    /** 状态：0待审批 1已通过/在访 2已拒绝 3已完成 4已取消 5已过期 */
    private Integer status;

    /** 探视日期起止 */
    private String beginVisitDate;
    private String endVisitDate;
}
