package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.ExamItemQuery;

/**
 * <p>
 * 体检项目表 服务类
 * </p>
 */
public interface IExamItemService extends IService<ExamItem> {

    IPage<ExamItem> list(ExamItemQuery examItemQuery);

    void add(ExamItem examItem);
}
