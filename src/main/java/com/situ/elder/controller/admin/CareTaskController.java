package com.situ.elder.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.query.CareTaskQuery;
import com.situ.elder.pojo.vo.CareTaskVO;
import com.situ.elder.service.ICareTaskService;
import com.situ.elder.service.IUserService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 护理任务与打卡记录表 前端控制器
 * </p>
 *
 * 护理任务由护理计划生成，不提供新增接口
 */
@RestController
@RequestMapping("/admin/care-task")
public class CareTaskController {

    @Autowired
    private ICareTaskService careTaskService;
    @Autowired
    private IUserService userService;

    private boolean isHugong(Long userId) {
        return userService.listRoleCodes(userId).contains("hugong");
    }

    private Long currentUserId(String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        if (id == null) {
            throw new ServiceException("登录已过期，请重新登录");
        }
        return id.longValue();
    }

    /**
     * 分页查询护理任务列表（附带老人、护理员、护理计划名称）
     * GET /care-task?page=1&limit=10&elderId=1&status=0
     * 护工（hugong）角色登录时只能看到分配给自己的任务
     */
    @GetMapping
    public Result<IPage<CareTaskVO>> list(CareTaskQuery careTaskQuery,
                                          @RequestHeader("Authorization") String token) {
        Long currentUserId = currentUserId(token);
        if (isHugong(currentUserId)) {
            careTaskQuery.setUserId(currentUserId);
        }
        IPage<CareTaskVO> page = careTaskService.list(careTaskQuery);
        return Result.ok(page);
    }

    /**
     * 根据ID查询护理任务
     * GET /care-task/1
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        CareTask careTask = careTaskService.getById(id);
        if (careTask == null) {
            throw new ServiceException("护理任务不存在");
        }
        if (isHugong(currentUserId(token)) && !careTask.getUserId().equals(currentUserId(token))) {
            throw new ServiceException("只能查看分配给您的护理任务");
        }
        return Result.ok(careTask);
    }

    /**
     * 修改护理任务（状态、执行打卡信息）；护工仅可回填分配给自己的任务
     * PUT /care-task/1
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody CareTask careTask,
                         @RequestHeader("Authorization") String token) {
        Long currentUserId = currentUserId(token);
        CareTask existing = careTaskService.getById(id);
        if (existing == null) {
            throw new ServiceException("护理任务不存在");
        }
        if (isHugong(currentUserId) && !existing.getUserId().equals(currentUserId)) {
            throw new ServiceException("只能回填分配给您的护理任务");
        }
        careTask.setId(id);
        careTaskService.updateById(careTask);
        return Result.ok("修改成功");
    }

    /**
     * 根据ID删除护理任务（护工不可删除）
     * DELETE /care-task/1
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (isHugong(currentUserId(token))) {
            throw new ServiceException("护工账号无删除权限");
        }
        careTaskService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除护理任务（护工不可删除）
     * DELETE /care-task
     */
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids, @RequestHeader("Authorization") String token) {
        if (isHugong(currentUserId(token))) {
            throw new ServiceException("护工账号无删除权限");
        }
        careTaskService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}
