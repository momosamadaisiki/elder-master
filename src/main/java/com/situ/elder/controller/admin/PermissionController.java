package com.situ.elder.controller.admin;


import com.situ.elder.pojo.entity.Permission;
import com.situ.elder.pojo.vo.PermissionVO;
import com.situ.elder.service.IPermissionService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 权限表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/admin/permissions")
public class PermissionController {
    @Autowired
    private IPermissionService permissionService;

    @GetMapping("/selectPermissionTree")
    public Result<List<PermissionVO>> selectPermissionTree() {
        List<PermissionVO> list = permissionService.selectPermissionTree();
        return Result.ok(list);
    }

    @PutMapping
    public Result update(@RequestBody Permission permission) {
        permissionService.updateById(permission);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        permissionService.removeById(id);
        return Result.ok("删除成功");
    }

    @PostMapping
    public Result add(@RequestBody Permission permission) {
        permissionService.save(permission);
        return Result.ok("添加成功");
    }

    @GetMapping("/{id}")
    public Result<Permission> selectById(@PathVariable Integer id) {
        Permission permission = permissionService.getById(id);
        return Result.ok(permission);
    }

}

