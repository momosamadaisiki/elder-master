package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.entity.CarePlan;
import com.situ.elder.pojo.dto.CarePlanDTO;
import com.situ.elder.pojo.query.CarePlanQuery;
import com.situ.elder.pojo.vo.CarePlanVO;

/**
 * <p>
 * 护理计划表 服务类
 * </p>
 */
public interface ICarePlanService extends IService<CarePlan> {

    IPage<CarePlanVO> list(CarePlanQuery carePlanQuery);

    void add(CarePlanDTO carePlanDTO);

    void update(Long id, CarePlanDTO carePlanDTO);

    /**
     * 根据ID查询护理计划及其护理项目明细
     */
    CarePlanDTO selectByIdWithItems(Long id);
}
