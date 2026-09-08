package com.situ.elder.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.dto.CheckInRequest;
import com.situ.elder.pojo.dto.CheckOutRequest;
import com.situ.elder.pojo.dto.TransferRequest;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ElderStay;
import com.situ.elder.pojo.query.StayQuery;
import com.situ.elder.service.IStayService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 入住/退住/结算
 */
@RestController
@RequestMapping("/admin/stays")
public class StayController {

    @Autowired
    private IStayService stayService;

    @GetMapping
    public Result<IPage<ElderStay>> list(StayQuery query) {
        return Result.ok(stayService.pageStays(query));
    }

    /** 可办理入住的老人 */
    @GetMapping("/candidates")
    public Result<List<Elder>> candidates() {
        return Result.ok(stayService.candidateElders());
    }

    /** 办理入住 */
    @PostMapping("/check-in")
    public Result checkIn(@RequestBody CheckInRequest request, @RequestHeader("Authorization") String token) {
        stayService.checkIn(request, currentUserId(token));
        return Result.ok("入住成功，已分配床位");
    }

    /** 调床 */
    @PutMapping("/{id}/transfer")
    public Result transfer(@PathVariable Long id, @RequestBody TransferRequest request) {
        stayService.transfer(id, request);
        return Result.ok("调床成功");
    }

    /** 退住（自动结算） */
    @PutMapping("/{id}/check-out")
    public Result checkOut(@PathVariable Long id, @RequestBody(required = false) CheckOutRequest request,
                           @RequestHeader("Authorization") String token) {
        BigDecimal total = stayService.checkOut(id, request, currentUserId(token));
        return Result.ok("退住成功，本次结算金额 ¥" + total + "（含护理费与床位费，状态待缴费）");
    }

    /** 结算缴费 */
    @PutMapping("/{id}/pay")
    public Result pay(@PathVariable Long id) {
        stayService.pay(id);
        return Result.ok("缴费成功");
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
