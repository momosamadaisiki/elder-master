package com.situ.elder.controller.admin;

import com.situ.elder.pojo.dto.ChargeAddRequest;
import com.situ.elder.pojo.entity.ElderCharge;
import com.situ.elder.service.IElderProfileService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 老人收费项目 + 月度账单
 */
@RestController
@RequestMapping("/admin/elder-charges")
public class ElderChargeController {

    @Autowired
    private IElderProfileService elderProfileService;

    /** 收费项目列表（可按月份过滤） */
    @GetMapping
    public Result<List<ElderCharge>> list(@RequestParam("elderId") Long elderId,
                                          @RequestParam(required = false) String month) {
        return Result.ok(elderProfileService.chargeList(elderId, month));
    }

    @PostMapping
    public Result add(@RequestBody ChargeAddRequest request) {
        elderProfileService.chargeAdd(request);
        return Result.ok("添加成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        elderProfileService.chargeDelete(id);
        return Result.ok("删除成功");
    }

    /** 月度账单（基础月费自动带出 + 收费项） */
    @GetMapping("/bill")
    public Result<Map<String, Object>> bill(@RequestParam("elderId") Long elderId,
                                            @RequestParam(required = false) String month) {
        return Result.ok(elderProfileService.bill(elderId, month));
    }
}
