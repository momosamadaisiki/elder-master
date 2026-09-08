package com.situ.elder.pojo.query;

import lombok.Data;

/**
 * 后台体检预约查询条件
 */
@Data
public class ExamAppointmentAdminQuery {

    private Integer page = 1;
    private Integer limit = 10;

    /** 老人姓名 */
    private String elderName;

    /** 0待体检 1体检中 2已完成 3已取消 4已过期 */
    private Integer status;

    private String beginDate;
    private String endDate;
}
