package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.ExamItem;
import com.situ.elder.pojo.entity.ExamPackage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 体检套餐详情VO（老人手机端）：套餐信息连同包含的体检项目明细
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamPackageDetailVO extends ExamPackage {

    /**
     * 包含的体检项目列表（按套餐内排序）
     */
    private List<ExamItem> examItemList;

}
