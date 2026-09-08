package com.situ.elder.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.listener.UserExcelListener;
import com.situ.elder.mapper.RoleMapper;
import com.situ.elder.mapper.UserRoleMapper;
import com.situ.elder.pojo.entity.*;
import com.situ.elder.mapper.UserMapper;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.pojo.vo.UserExcelVO;
import com.situ.elder.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    //private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public IPage<User> list(UserQuery userQuery) {
        IPage<User> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        /*QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!ObjectUtils.isEmpty(userQuery.getName())) {
            queryWrapper.like("name", userQuery.getName());
        }
        if (!ObjectUtils.isEmpty(userQuery.getEmail())) {
            queryWrapper.like("email", userQuery.getEmail());
        }*/
        //queryWrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), "name", userQuery.getName());
        //queryWrapper.like(!ObjectUtils.isEmpty(userQuery.getEmail()), "email", userQuery.getEmail());

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), User::getName, userQuery.getName())
                .like(!ObjectUtils.isEmpty(userQuery.getEmail()), User::getEmail, userQuery.getEmail())
                .between(!ObjectUtils.isEmpty(userQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(userQuery.getEndCreateTime()), User::getCreateTime, userQuery.getBeginCreateTime(), userQuery.getEndCreateTime())
                .orderByDesc(User::getCreateTime);

        return userMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public List<User> listByRoleCode(String roleCode) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role == null) {
            return List.of();
        }
        List<Long> userIdList = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId()))
                .stream().map(UserRole::getUserId).toList();
        if (userIdList.isEmpty()) {
            return List.of();
        }
        List<User> userList = userMapper.selectBatchIds(userIdList);
        //下拉选择不需要密码
        userList.forEach(user -> user.setPassword(null));
        return userList;
    }

    @Override
    public boolean hasRoleCode(Long userId, String roleCode) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role == null) {
            return false;
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId).eq(UserRole::getRoleId, role.getId()));
        return count > 0;
    }

    @Override
    public List<String> listRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)).stream()
                .map(UserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(Role::getCode).distinct().toList();
    }

    @Override
    public void exportExcel(HttpServletResponse response) {
        List<User> list = userMapper.selectList(null);
        /*List<UserExcelVO> userExcelVOList = new ArrayList<>();
        for (User user : list) {
            UserExcelVO userExcelVO = new UserExcelVO();
            BeanUtils.copyProperties(user, userExcelVO);
            userExcelVOList.add(userExcelVO);
        }*/

        List<UserExcelVO> userExcelVOList = list.stream().map(user -> {
            UserExcelVO userExcelVO = new UserExcelVO();
            BeanUtils.copyProperties(user, userExcelVO);
            return userExcelVO;
        }).toList();

        ExcelUtil.exportExcel(response, userExcelVOList, UserExcelVO.class, "用户信息表");
    }

    @Override
    public void importExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), UserExcelVO.class, new UserExcelListener(userMapper)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(User user) {
//        log.error("error");
//        log.warn("warn");
//        log.info("info");
//        log.debug("debug");
        log.info("添加用户：{}", user);
        User userInDB = userMapper.selectOne(new QueryWrapper<User>().eq("name", user.getName()));
        log.info("用户在数据库中的信息：{}", userInDB);
        if (userInDB != null) {
            throw new ServiceException("用户名已存在");
        }
        //密码 BCrypt 加密；为空时给默认密码 123456
        String raw = (user.getPassword() == null || user.getPassword().isEmpty()) ? "123456" : user.getPassword();
        user.setPassword(com.situ.elder.util.PasswordUtil.encode(raw));
        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> selectAssignedRole(Long userId) {
        //查询所有的角色
        List<Role> roleList = roleMapper.selectList(null);
        //查询这个老人已分配的tag id
        LambdaQueryWrapper<UserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserRole::getUserId, userId);
        List<Long> assignedRoleIdList = userRoleMapper.selectList(lambdaQueryWrapper).stream()
                .map(UserRole::getRoleId).toList();

        Map<String, Object> map = new HashMap<>();
        map.put("roleList", roleList);
        map.put("assignedRoleIdList", assignedRoleIdList);
        return map;
    }

    @Override
    public void assignRole(Long userId, Long[] roleIds) {
        LambdaQueryWrapper<UserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(lambdaQueryWrapper);
        //在user_role表中添加这个用户新的角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
