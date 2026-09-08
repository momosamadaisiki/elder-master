package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.VisitRecord;
import com.situ.elder.pojo.query.VisitQuery;
import com.situ.elder.pojo.vo.VisitVO;

/**
 * 探视/访客服务
 */
public interface IVisitService {

    /** 分页列表（回填老人姓名） */
    IPage<VisitVO> list(VisitQuery visitQuery);

    /** 新增申请/现场登记（现场登记立即置为已通过并记录到达时间） */
    void add(VisitRecord visitRecord);

    /** 审批通过（仅待审批） */
    void approve(Long id, Long adminUserId);

    /** 审批拒绝（仅待审批），remark 为拒绝意见 */
    void reject(Long id, Long adminUserId, String remark);

    /** 到达登记（仅已通过且未离院） */
    void arrive(Long id);

    /** 离院登记（仅已通过/在访且已登记到达） */
    void leave(Long id);

    /** 手动置过期（仅待审批） */
    void expire(Long id);

    /** 删除（仅未生效/已拒绝/已取消/已过期记录） */
    void deleteById(Long id);
}
