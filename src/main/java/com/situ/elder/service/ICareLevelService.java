package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareLevel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.CareLevelQuery;

/**
 * <p>
 * 护理等级表 服务类
 * </p>
 */
public interface ICareLevelService extends IService<CareLevel> {

    IPage<CareLevel> list(CareLevelQuery careLevelQuery);

    void add(CareLevel careLevel);
}
