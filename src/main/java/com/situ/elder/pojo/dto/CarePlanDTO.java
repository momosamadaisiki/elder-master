package com.situ.elder.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.situ.elder.pojo.entity.CarePlan;
import com.situ.elder.pojo.entity.CarePlanItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 护理计划DTO：护理计划及其包含的护理项目明细，用于新增、修改、回显
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CarePlanDTO extends CarePlan {

    /**
     * 护理项目明细列表
     */
    @TableField(exist = false)
    private List<CarePlanItem> carePlanItemList;

}
