package com.situ.elder.pojo.query;

import lombok.Data;

/**
 * 入住单查询
 */
@Data
public class StayQuery {

    private Integer page = 1;
    private Integer limit = 10;

    /** 老人姓名关键字 */
    private String elderName;

    /** true=仅看当前在住 */
    private Boolean activeOnly;

    private String beginDate;
    private String endDate;
}
