package com.situ.elder.service.impl;

import com.situ.elder.pojo.entity.UserRole;
import com.situ.elder.mapper.UserRoleMapper;
import com.situ.elder.service.IUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工-角色关联表 服务实现类
 * </p>
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

}
