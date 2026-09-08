package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.UserQuery;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    IPage<User> list(UserQuery userQuery);

    List<User> listByRoleCode(String roleCode);

    boolean hasRoleCode(Long userId, String roleCode);

    /** 当前用户拥有的全部角色编码 */
    List<String> listRoleCodes(Long userId);

    void exportExcel(HttpServletResponse response);

    void importExcel(MultipartFile file);

    void add(User user);

    Map<String, Object> selectAssignedRole(Long userId);

    void assignRole(Long userId, Long[] roleIds);
}
