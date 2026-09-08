package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.UserPasswordDTO;
import com.situ.elder.pojo.entity.User;
import com.situ.elder.pojo.query.UserQuery;
import com.situ.elder.service.IPermissionService;
import com.situ.elder.service.IUserService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.PasswordUtil;
import com.situ.elder.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 */
@RestController
@RequestMapping("/admin/users")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IPermissionService permissionService;

    // /users/assignRole?userId=1&roleIds=1,2,3
    @PostMapping("/assignRole")
    public Result assignRole(@RequestParam("userId") Long userId, @RequestParam("roleIds") Long[] roleIds) {
        userService.assignRole(userId, roleIds);
        return Result.ok("分配角色成功");
    }

    @GetMapping("/selectAssignedRole/{userId}")
    public Result selectAssignedRole(@PathVariable("userId") Long userId) {
        Map<String, Object> map = userService.selectAssignedRole(userId);
        return Result.ok(map);
    }

    //根据角色编码查询用户列表，如 /users/listByRoleCode/hugong
    @GetMapping("/listByRoleCode/{roleCode}")
    public Result<List<User>> listByRoleCode(@PathVariable("roleCode") String roleCode) {
        return Result.ok(userService.listByRoleCode(roleCode));
    }

    //导出
    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse response) {
        userService.exportExcel(response);
    }

    //导入
    @PostMapping("/importExcel")
    public Result importExcel( MultipartFile file) {
        userService.importExcel(file);
        return Result.ok("导入成功");
    }



    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestHeader("Authorization") String token,
                                @RequestBody UserPasswordDTO userPasswordDTO) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        User user = userService.getById(id);
        String stored = user.getPassword();
        //兼容历史明文：库中为明文且与旧密码一致视为通过，并在此时升级为 BCrypt
        boolean oldOk = PasswordUtil.matches(userPasswordDTO.getOldPassword(), stored)
                || (!PasswordUtil.isBcrypt(stored) && stored != null
                && stored.equalsIgnoreCase(userPasswordDTO.getOldPassword()));
        if (!oldOk) {
            return Result.error("旧密码错误");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(PasswordUtil.encode(userPasswordDTO.getNewPassword()));
        userService.updateById(updateUser);
        return Result.ok("密码修改成功");

    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        User user = userService.getById(id);
        user.setPassword(null);

        Map<String, Object> permissionMap = permissionService.selectPermissionByUserId(user.getId());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", user);
        resultMap.put("routerList", permissionMap.get("routerList"));
        resultMap.put("btnList", permissionMap.get("btnList"));
        resultMap.put("roleCodes", userService.listRoleCodes(user.getId()));
        return Result.ok(resultMap);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        // 根据用户名查找这个用户
        User dbUser = userService.getOne(new QueryWrapper<User>().eq("name", user.getName()));
        if (dbUser == null) {
            return Result.error("用户名不存在");
        }
        String stored = dbUser.getPassword();
        //校验：BCrypt 密文或历史明文（明文将在登录成功后自动升级）
        boolean passwordOk = PasswordUtil.matches(user.getPassword(), stored)
                || (!PasswordUtil.isBcrypt(stored) && stored != null
                && stored.equalsIgnoreCase(user.getPassword()));
        if (!passwordOk) {
            return Result.error("密码错误");
        }
        // 登录成功后，判断用户是否被禁用
        if (dbUser.getStatus() == 0) {
            return Result.error("用户已禁用");
        }

        //明文密码升级为 BCrypt（一次性）
        if (!PasswordUtil.isBcrypt(stored)) {
            User upgrade = new User();
            upgrade.setId(dbUser.getId());
            upgrade.setPassword(PasswordUtil.encode(user.getPassword()));
            userService.updateById(upgrade);
        }

        // 登录成功，生成token
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbUser.getId());
        map.put("name", dbUser.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功", token);
    }

    /**
     * 分页查询用户列表
     * GET /users?page=1&limit=10&name=xxx&phone=xxx
     */
    @GetMapping
    public Result<IPage<User>> list(UserQuery userQuery) {
        IPage<User> page = userService.list(userQuery);
        return Result.ok(page);
    }

    /**
     * 根据ID查询用户
     * GET /users/1
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    /**
     * 新增用户
     * POST /users
     */
    @PostMapping
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.ok("新增成功");
    }

    /**
     * 修改用户
     * PUT /users/1
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody User user) {
        //密码：非空则加密存储；留空则不修改
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setId(id);
        userService.updateById(user);
        return Result.ok("修改成功");
    }

    /**
     * 根据ID删除用户（逻辑删除）
     * DELETE /users/1
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除用户
     * DELETE /users
     */
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        userService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}

