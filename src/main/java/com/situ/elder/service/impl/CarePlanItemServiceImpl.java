package com.situ.elder.service.impl;

import com.situ.elder.pojo.entity.CarePlanItem;
import com.situ.elder.mapper.CarePlanItemMapper;
import com.situ.elder.service.ICarePlanItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 护理计划和项目关联表 服务实现类
 * </p>
 */
@Service
public class CarePlanItemServiceImpl extends ServiceImpl<CarePlanItemMapper, CarePlanItem> implements ICarePlanItemService {

}
