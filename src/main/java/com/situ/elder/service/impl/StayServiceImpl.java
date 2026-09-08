package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.CareLevelMapper;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.mapper.ElderStayMapper;
import com.situ.elder.mapper.FeeSettlementMapper;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.pojo.dto.CheckInRequest;
import com.situ.elder.pojo.dto.CheckOutRequest;
import com.situ.elder.pojo.dto.TransferRequest;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.CareLevel;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ElderStay;
import com.situ.elder.pojo.entity.FeeSettlement;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.StayQuery;
import com.situ.elder.service.IStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StayServiceImpl extends ServiceImpl<ElderStayMapper, ElderStay> implements IStayService {

    private static final BigDecimal DAYS_PER_MONTH = BigDecimal.valueOf(30);

    @Autowired
    private ElderStayMapper stayMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private CareLevelMapper careLevelMapper;
    @Autowired
    private FeeSettlementMapper settlementMapper;

    @Override
    public IPage<ElderStay> pageStays(StayQuery query) {
        IPage<ElderStay> page = new Page<>(query.getPage(), query.getLimit());
        LambdaQueryWrapper<ElderStay> wrapper = new LambdaQueryWrapper<>();
        if (!ObjectUtils.isEmpty(query.getElderName())) {
            List<Long> elderIds = elderMapper.selectList(new LambdaQueryWrapper<Elder>()
                            .like(Elder::getName, query.getElderName())).stream()
                    .map(Elder::getId).toList();
            if (elderIds.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            wrapper.in(ElderStay::getElderId, elderIds);
        }
        if (Boolean.TRUE.equals(query.getActiveOnly())) {
            wrapper.isNull(ElderStay::getCheckOutDate);
        }
        wrapper.ge(!ObjectUtils.isEmpty(query.getBeginDate()), ElderStay::getCheckInDate, query.getBeginDate())
                .le(!ObjectUtils.isEmpty(query.getEndDate()), ElderStay::getCheckInDate, query.getEndDate())
                .orderByDesc(ElderStay::getCheckInDate)
                .orderByDesc(ElderStay::getId);

        IPage<ElderStay> result = stayMapper.selectPage(page, wrapper);
        List<ElderStay> records = result.getRecords();
        attachInfo(records);
        return result;
    }

    /** 批量回填：老人名/护理等级名/床位房间/结算 */
    private void attachInfo(List<ElderStay> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> elderIds = records.stream().map(ElderStay::getElderId).distinct().toList();
        List<Long> levelIds = records.stream().map(ElderStay::getCareLevelId)
                .filter(l -> !ObjectUtils.isEmpty(l)).distinct().toList();
        List<Long> bedIds = records.stream().map(ElderStay::getBedId).distinct().toList();
        Map<Long, String> elderName = elderMapper.selectBatchIds(elderIds).stream()
                .collect(Collectors.toMap(Elder::getId, Elder::getName));
        Map<Long, String> levelName = levelIds.isEmpty() ? Map.of()
                : careLevelMapper.selectBatchIds(levelIds).stream()
                .collect(Collectors.toMap(CareLevel::getId, CareLevel::getName));
        Map<Long, Bed> bedMap = bedIds.isEmpty() ? Map.of()
                : bedMapper.selectBatchIds(bedIds).stream().collect(Collectors.toMap(Bed::getId, b -> b));
        List<Long> roomIds = bedMap.values().stream().map(Bed::getRoomId).distinct().toList();
        Map<Long, Room> roomMap = roomIds.isEmpty() ? Map.of()
                : roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(Room::getId, r -> r));

        List<Long> stayIds = records.stream().map(ElderStay::getId).toList();
        Map<Long, FeeSettlement> settlementMap = stayIds.isEmpty() ? Map.of()
                : settlementMapper.selectList(new LambdaQueryWrapper<FeeSettlement>().in(FeeSettlement::getStayId, stayIds))
                .stream().collect(Collectors.toMap(FeeSettlement::getStayId, s -> s));

        records.forEach(stay -> {
            stay.setElderName(elderName.get(stay.getElderId()));
            stay.setCareLevelName(levelName.get(stay.getCareLevelId()));
            Bed bed = bedMap.get(stay.getBedId());
            if (bed != null) {
                stay.setBedNo(bed.getBedNo());
                Room room = roomMap.get(bed.getRoomId());
                stay.setRoomCode(room == null ? "" : room.getFloor() + "楼" + room.getCode());
            }
            FeeSettlement settlement = settlementMap.get(stay.getId());
            if (settlement != null) {
                stay.setSettlementId(settlement.getId());
                stay.setSettleAmount(settlement.getTotalAmount());
                stay.setSettleStatus(settlement.getStatus());
            }
        });
    }

    @Override
    public List<Elder> candidateElders() {
        List<Long> activeElderIds = stayMapper.selectList(new LambdaQueryWrapper<ElderStay>()
                        .isNull(ElderStay::getCheckOutDate)).stream()
                .map(ElderStay::getElderId).toList();
        List<Elder> elders = elderMapper.selectList(null);
        if (activeElderIds.isEmpty()) {
            return elders;
        }
        List<Elder> result = new ArrayList<>();
        for (Elder elder : elders) {
            if (!activeElderIds.contains(elder.getId())) {
                result.add(elder);
            }
        }
        //不返回敏感信息
        result.forEach(e -> e.setPassword(null));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkIn(CheckInRequest request, Long operatorId) {
        if (request == null || request.elderId() == null || request.bedId() == null) {
            throw new ServiceException("请选择老人与床位");
        }
        Elder elder = elderMapper.selectById(request.elderId());
        if (elder == null) {
            throw new ServiceException("老人不存在");
        }
        long activeCount = stayMapper.selectCount(new LambdaQueryWrapper<ElderStay>()
                .eq(ElderStay::getElderId, elder.getId()).isNull(ElderStay::getCheckOutDate));
        if (activeCount > 0) {
            throw new ServiceException("该老人当前已在住，不能重复办理入住");
        }
        Bed bed = bedMapper.selectById(request.bedId());
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        if (bed.getStatus() == null || bed.getStatus() != RoomServiceImpl.BED_FREE) {
            throw new ServiceException("该床位当前不可用，请选择空闲床位");
        }
        LocalDate checkInDate = parseDate(request.checkInDate(), LocalDate.now());

        ElderStay stay = new ElderStay();
        stay.setElderId(elder.getId());
        stay.setBedId(bed.getId());
        stay.setCareLevelId(request.careLevelId());
        stay.setCheckInDate(Date.valueOf(checkInDate));
        stay.setOperatorId(operatorId);
        stay.setRemark(request.remark());
        stayMapper.insert(stay);

        bed.setStatus(RoomServiceImpl.BED_OCCUPIED);
        bedMapper.updateById(bed);

        Elder elderUpdate = new Elder();
        elderUpdate.setId(elder.getId());
        elderUpdate.setStatus(4); //入住中
        elderMapper.updateById(elderUpdate);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void transfer(Long stayId, TransferRequest request) {
        ElderStay stay = requireActiveStay(stayId);
        Bed newBed = requireBed(request == null ? null : request.bedId());
        if (newBed.getStatus() == null || newBed.getStatus() != RoomServiceImpl.BED_FREE) {
            throw new ServiceException("目标床位当前不可用");
        }
        if (newBed.getId().equals(stay.getBedId())) {
            throw new ServiceException("目标床位与原床位相同");
        }
        Bed oldBed = bedMapper.selectById(stay.getBedId());
        //新床占用
        newBed.setStatus(RoomServiceImpl.BED_OCCUPIED);
        bedMapper.updateById(newBed);
        //旧床释放
        if (oldBed != null) {
            oldBed.setStatus(RoomServiceImpl.BED_FREE);
            bedMapper.updateById(oldBed);
        }
        stay.setBedId(newBed.getId());
        stayMapper.updateById(stay);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BigDecimal checkOut(Long stayId, CheckOutRequest request, Long operatorId) {
        ElderStay stay = requireActiveStay(stayId);
        LocalDate today = LocalDate.now();
        LocalDate checkInLocal = stay.getCheckInDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(checkInLocal, today) + 1;
        if (days < 1) {
            days = 1;
        }

        Bed bed = bedMapper.selectById(stay.getBedId());
        Room room = bed == null ? null : roomMapper.selectById(bed.getRoomId());
        CareLevel level = stay.getCareLevelId() == null ? null : careLevelMapper.selectById(stay.getCareLevelId());

        BigDecimal careAmount = calcMonth(level == null ? null : level.getPrice(), days);
        BigDecimal bedAmount = calcMonth(room == null || room.getPrice() == null ? null : room.getPrice(), days);
        BigDecimal total = careAmount.add(bedAmount).setScale(2, RoundingMode.HALF_UP);

        //1) 释放床位
        if (bed != null) {
            bed.setStatus(RoomServiceImpl.BED_FREE);
            bedMapper.updateById(bed);
        }
        //2) 老人状态 → 已退住
        Elder elderUpdate = new Elder();
        elderUpdate.setId(stay.getElderId());
        elderUpdate.setStatus(5);
        elderMapper.updateById(elderUpdate);
        //3) 入住单归档
        stay.setCheckOutDate(Date.valueOf(today));
        stay.setOperatorId(operatorId);
        if (!ObjectUtils.isEmpty(request == null ? null : request.remark())) {
            stay.setRemark(request.remark());
        }
        stayMapper.updateById(stay);
        //4) 生成结算单
        FeeSettlement settlement = new FeeSettlement();
        settlement.setElderId(stay.getElderId());
        settlement.setStayId(stay.getId());
        settlement.setDays((int) days);
        settlement.setCareLevelName(level == null ? null : level.getName());
        settlement.setCareAmount(careAmount);
        settlement.setBedInfo(room == null ? "" : room.getFloor() + "楼" + room.getCode()
                + (bed == null ? "" : "/" + bed.getBedNo()));
        settlement.setBedAmount(bedAmount);
        settlement.setTotalAmount(total);
        settlement.setStatus(0);
        settlementMapper.insert(settlement);
        return total;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pay(Long stayId) {
        FeeSettlement settlement = settlementMapper.selectOne(new LambdaQueryWrapper<FeeSettlement>()
                .eq(FeeSettlement::getStayId, stayId));
        if (settlement == null) {
            throw new ServiceException("该入住单没有待结算的费用");
        }
        if (settlement.getStatus() == 1) {
            throw new ServiceException("该结算单已结清");
        }
        settlement.setStatus(1);
        settlement.setSettleTime(new java.util.Date());
        settlementMapper.updateById(settlement);
    }

    private ElderStay requireActiveStay(Long stayId) {
        ElderStay stay = stayMapper.selectById(stayId);
        if (stay == null) {
            throw new ServiceException("入住单不存在");
        }
        if (stay.getCheckOutDate() != null) {
            throw new ServiceException("该入住单已退住");
        }
        return stay;
    }

    private Bed requireBed(Long bedId) {
        if (bedId == null) {
            throw new ServiceException("请选择床位");
        }
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        return bed;
    }

    private BigDecimal calcMonth(BigDecimal monthly, long days) {
        if (monthly == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return monthly.divide(DAYS_PER_MONTH, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate parseDate(String date, LocalDate fallback) {
        if (ObjectUtils.isEmpty(date)) {
            return fallback;
        }
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            throw new ServiceException("日期格式错误：" + date);
        }
    }
}
