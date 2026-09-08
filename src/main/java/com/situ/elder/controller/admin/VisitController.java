package com.situ.elder.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.entity.VisitRecord;
import com.situ.elder.pojo.query.VisitQuery;
import com.situ.elder.pojo.vo.VisitVO;
import com.situ.elder.service.IVisitService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 探视/访客登记（后台管理）
 */
@RestController
@RequestMapping("/admin/visits")
public class VisitController {

    @Autowired
    private IVisitService visitService;

    /** 分页列表 */
    @GetMapping
    public Result<IPage<VisitVO>> list(VisitQuery visitQuery) {
        return Result.ok(visitService.list(visitQuery));
    }

    /** 新增（0=线上申请/默认待审批；1=现场登记，直接到访） */
    @PostMapping
    public Result add(@RequestBody VisitRecord visitRecord) {
        visitService.add(visitRecord);
        return Result.ok("新增成功");
    }

    /** 审批通过 */
    @PutMapping("/{id}/approve")
    public Result approve(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        visitService.approve(id, currentUserId(token));
        return Result.ok("已通过");
    }

    /** 审批拒绝 */
    @PutMapping("/{id}/reject")
    public Result reject(@PathVariable Long id,
                         @RequestHeader("Authorization") String token,
                         @RequestParam(required = false) String remark) {
        visitService.reject(id, currentUserId(token), remark);
        return Result.ok("已拒绝");
    }

    /** 到达登记 */
    @PutMapping("/{id}/arrive")
    public Result arrive(@PathVariable Long id) {
        visitService.arrive(id);
        return Result.ok("已登记到达");
    }

    /** 离院登记 */
    @PutMapping("/{id}/leave")
    public Result leave(@PathVariable Long id) {
        visitService.leave(id);
        return Result.ok("已登记离院");
    }

    /** 手动置过期 */
    @PutMapping("/{id}/expire")
    public Result expire(@PathVariable Long id) {
        visitService.expire(id);
        return Result.ok("已置为过期");
    }

    /** 删除（仅待审批/已拒绝/已取消/已过期） */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        visitService.deleteById(id);
        return Result.ok("删除成功");
    }

    private Long currentUserId(String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        if (id == null) {
            throw new ServiceException("登录已过期，请重新登录");
        }
        return id.longValue();
    }
}
