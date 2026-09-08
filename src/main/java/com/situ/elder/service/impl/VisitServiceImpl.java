package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.mapper.VisitMapper;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.VisitRecord;
import com.situ.elder.pojo.query.VisitQuery;
import com.situ.elder.pojo.vo.VisitVO;
import com.situ.elder.service.IVisitService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 探视/访客：状态机 0待审批→1已通过→(到达)→1在访→(离院)→3已完成；
 * 0→2拒绝；0→4取消；0→5过期。所有流转都校验当前状态，防止并发重复操作。
 */
@Service
public class VisitServiceImpl extends ServiceImpl<VisitMapper, VisitRecord> implements IVisitService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_CANCELED = 4;
    public static final int STATUS_EXPIRED = 5;

    @Autowired
    private VisitMapper visitMapper;
    @Autowired
    private ElderMapper elderMapper;

    @Override
    public IPage<VisitVO> list(VisitQuery visitQuery) {
        IPage<VisitRecord> page = new Page<>(visitQuery.getPage(), visitQuery.getLimit());
        LambdaQueryWrapper<VisitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(visitQuery.getVisitorName()), VisitRecord::getVisitorName, visitQuery.getVisitorName())
                .eq(!ObjectUtils.isEmpty(visitQuery.getElderId()), VisitRecord::getElderId, visitQuery.getElderId())
                .eq(!ObjectUtils.isEmpty(visitQuery.getStatus()), VisitRecord::getStatus, visitQuery.getStatus())
                .ge(!ObjectUtils.isEmpty(visitQuery.getBeginVisitDate()), VisitRecord::getVisitDate, visitQuery.getBeginVisitDate())
                .le(!ObjectUtils.isEmpty(visitQuery.getEndVisitDate()), VisitRecord::getVisitDate, visitQuery.getEndVisitDate())
                .orderByDesc(VisitRecord::getVisitDate)
                .orderByDesc(VisitRecord::getCreateTime);

        IPage<VisitRecord> resultPage = visitMapper.selectPage(page, wrapper);
        List<VisitRecord> records = resultPage.getRecords();

        //批量回填老人姓名（防 N+1）
        List<Long> elderIds = records.stream().map(VisitRecord::getElderId).filter(e -> !ObjectUtils.isEmpty(e)).distinct().toList();
        Map<Long, String> idToName = ObjectUtils.isEmpty(elderIds) ? Map.of()
                : elderMapper.selectBatchIds(elderIds).stream().collect(Collectors.toMap(Elder::getId, Elder::getName));

        List<VisitVO> voList = records.stream().map(record -> {
            VisitVO vo = new VisitVO();
            BeanUtils.copyProperties(record, vo);
            vo.setElderName(idToName.get(record.getElderId()));
            return vo;
        }).toList();

        Page<VisitVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(VisitRecord visitRecord) {
        if (visitRecord.getElderId() == null) {
            throw new ServiceException("请选择被访老人");
        }
        if (ObjectUtils.isEmpty(visitRecord.getVisitorName())) {
            throw new ServiceException("请输入访客姓名");
        }
        if (visitRecord.getVisitDate() == null) {
            throw new ServiceException("请选择探视日期");
        }
        if (elderMapper.selectById(visitRecord.getElderId()) == null) {
            throw new ServiceException("被访老人不存在");
        }
        int applyWay = visitRecord.getApplyWay() == null ? 0 : visitRecord.getApplyWay();
        visitRecord.setApplyWay(applyWay);
        visitRecord.setId(null);
        if (applyWay == 1) {
            //现场登记：视为已通过且即刻到访
            visitRecord.setStatus(STATUS_APPROVED);
            visitRecord.setArriveTime(new Date());
        } else {
            visitRecord.setStatus(STATUS_PENDING);
        }
        visitMapper.insert(visitRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approve(Long id, Long adminUserId) {
        VisitRecord record = requireRecord(id);
        requireStatus(record, STATUS_PENDING, "仅待审批的申请可以通过");
        record.setStatus(STATUS_APPROVED);
        record.setApproveUserId(adminUserId);
        record.setApproveTime(new Date());
        visitMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reject(Long id, Long adminUserId, String remark) {
        VisitRecord record = requireRecord(id);
        requireStatus(record, STATUS_PENDING, "仅待审批的申请可以拒绝");
        record.setStatus(STATUS_REJECTED);
        record.setApproveUserId(adminUserId);
        record.setApproveTime(new Date());
        if (!ObjectUtils.isEmpty(remark)) {
            record.setRemark(remark);
        }
        visitMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void arrive(Long id) {
        VisitRecord record = requireRecord(id);
        if (record.getStatus() != STATUS_APPROVED) {
            throw new ServiceException("该记录不在“已通过/在访”状态，无法登记到达");
        }
        if (record.getArriveTime() != null) {
            throw new ServiceException("该访客已登记到达，请勿重复登记");
        }
        record.setArriveTime(new Date());
        visitMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void leave(Long id) {
        VisitRecord record = requireRecord(id);
        if (record.getStatus() != STATUS_APPROVED) {
            throw new ServiceException("该记录不在“已通过/在访”状态，无法登记离院");
        }
        if (record.getArriveTime() == null) {
            throw new ServiceException("尚未登记到达，请先登记到达再离院");
        }
        record.setLeaveTime(new Date());
        record.setStatus(STATUS_FINISHED);
        visitMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void expire(Long id) {
        VisitRecord record = requireRecord(id);
        requireStatus(record, STATUS_PENDING, "仅待审批的记录可以置为过期");
        record.setStatus(STATUS_EXPIRED);
        visitMapper.updateById(record);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(Long id) {
        VisitRecord record = requireRecord(id);
        if (record.getStatus() == STATUS_APPROVED || record.getStatus() == STATUS_FINISHED) {
            throw new ServiceException("已通过/已完成的历史记录不可删除");
        }
        visitMapper.deleteById(id);
    }

    private VisitRecord requireRecord(Long id) {
        VisitRecord record = visitMapper.selectById(id);
        if (record == null) {
            throw new ServiceException("探视记录不存在");
        }
        return record;
    }

    private void requireStatus(VisitRecord record, int expected, String message) {
        if (record.getStatus() == null || record.getStatus() != expected) {
            throw new ServiceException(message);
        }
    }
}
