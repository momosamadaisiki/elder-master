package com.situ.elder.service;

import com.situ.elder.pojo.dto.ChargeAddRequest;
import com.situ.elder.pojo.entity.ElderCharge;
import com.situ.elder.pojo.entity.ElderFamily;

import java.util.List;
import java.util.Map;

/**
 * 老人信息扩展：家属 / 收费项目 / 月度账单
 */
public interface IElderProfileService {

    // ===== 家属 =====
    List<ElderFamily> familyList(Long elderId);

    void familyAdd(ElderFamily family);

    void familyUpdate(Long id, ElderFamily family);

    void familyDelete(Long id);

    /** 家属登录：校验并自动升级密码，返回家属记录 */
    ElderFamily loginFamily(String name, String password);

    /** 家属只读视图：绑定老人的档案摘要 + 最近护理记录 */
    Map<String, Object> familyElderView(Long elderId);

    /** 根据家属记录ID获取其绑定老人的只读视图 */
    Map<String, Object> familyElderViewOfFamily(Long familyId);

    // ===== 收费项目 =====
    List<ElderCharge> chargeList(Long elderId, String month);

    void chargeAdd(ChargeAddRequest request);

    void chargeDelete(Long id);

    /**
     * 月度账单：基础月费(在住护理等级+床位月费，自动带出) + 该月收费项合计
     */
    Map<String, Object> bill(Long elderId, String month);
}
