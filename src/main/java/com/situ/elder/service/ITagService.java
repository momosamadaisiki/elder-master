package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.TagQuery;

/**
 * <p>
 * 标签表 服务类
 * </p>
 */
public interface ITagService extends IService<Tag> {

    IPage<Tag> list(TagQuery tagQuery);
}
