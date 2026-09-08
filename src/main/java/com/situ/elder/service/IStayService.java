package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.CheckInRequest;
import com.situ.elder.pojo.dto.CheckOutRequest;
import com.situ.elder.pojo.dto.TransferRequest;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ElderStay;
import com.situ.elder.pojo.query.StayQuery;

import java.math.BigDecimal;
import java.util.List;

/**
 * 入住/退住/结算服务
 */
public interface IStayService {

    /** 入住单分页（回填老人/床位/等级/结算信息） */
    IPage<ElderStay> pageStays(StayQuery query);

    /** 可办理入住的老人（无在住单） */
    List<Elder> candidateElders();

    /** 办理入住：占床 + 老人状态置入住中 */
    void checkIn(CheckInRequest request, Long operatorId);

    /** 调床：旧床释放、新床占用 */
    void transfer(Long stayId, TransferRequest request);

    /** 退住：释放床位 + 老人状态置已退住 + 生成结算单（护理费+床位费 按30天折算） */
    BigDecimal checkOut(Long stayId, CheckOutRequest request, Long operatorId);

    /** 结算缴费 */
    void pay(Long stayId);
}
