package com.situ.elder.service;

import com.situ.elder.pojo.entity.Permission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.vo.PermissionVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 权限表 服务类
 * </p>
 */
public interface IPermissionService extends IService<Permission> {

    List<PermissionVO> selectPermissionTree();

    Map<String, Object> selectPermissionByUserId(Long id);
}
