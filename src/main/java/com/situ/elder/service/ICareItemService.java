package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.CareItemQuery;

/**
 * <p>
 * 护理项目表 服务类
 * </p>
 */
public interface ICareItemService extends IService<CareItem> {

    IPage<CareItem> list(CareItemQuery careItemQuery);

    void add(CareItem careItem);
}
