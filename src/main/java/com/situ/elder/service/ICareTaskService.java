package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CareTaskQuery;
import com.situ.elder.pojo.vo.CareTaskVO;

/**
 * <p>
 * 护理任务与打卡记录表 服务类
 * </p>
 */
public interface ICareTaskService extends IService<CareTask> {

    IPage<CareTaskVO> list(CareTaskQuery careTaskQuery);
}
