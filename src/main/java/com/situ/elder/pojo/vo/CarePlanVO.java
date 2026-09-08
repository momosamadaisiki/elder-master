package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.CarePlan;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 护理计划列表VO：在护理计划信息基础上附带老人、护理人员、护理等级名称
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CarePlanVO extends CarePlan {

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 护理人员姓名
     */
    private String userName;

    /**
     * 护理等级名称
     */
    private String careLevelName;

}
