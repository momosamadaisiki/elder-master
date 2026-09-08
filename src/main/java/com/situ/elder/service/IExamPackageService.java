package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamPackage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.ExamPackageQuery;
import com.situ.elder.pojo.vo.ExamPackageDetailVO;
import com.situ.elder.pojo.vo.ExamPackageVO;

import java.util.List;

/**
 * <p>
 * 体检套餐表 服务类
 * </p>
 */
public interface IExamPackageService extends IService<ExamPackage> {

    IPage<ExamPackage> list(ExamPackageQuery examPackageQuery);

    void add(ExamPackage examPackage);

    /**
     * 查询上架的套餐列表（老人手机端，含项目数量）
     */
    List<ExamPackageVO> listOnShelf();

    /**
     * 查询套餐详情连同包含的体检项目（老人手机端，套餐须上架）
     */
    ExamPackageDetailVO selectDetailById(Long id);
}
