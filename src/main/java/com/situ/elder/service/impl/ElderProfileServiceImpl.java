package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.CareLevelMapper;
import com.situ.elder.mapper.CareTaskMapper;
import com.situ.elder.mapper.ElderChargeMapper;
import com.situ.elder.mapper.ElderFamilyMapper;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.mapper.ElderStayMapper;
import com.situ.elder.mapper.ElderTagMapper;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.mapper.TagMapper;
import com.situ.elder.pojo.dto.ChargeAddRequest;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.CareLevel;
import com.situ.elder.pojo.entity.CareTask;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.pojo.entity.ElderCharge;
import com.situ.elder.pojo.entity.ElderFamily;
import com.situ.elder.pojo.entity.ElderStay;
import com.situ.elder.pojo.entity.ElderTag;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.entity.Tag;
import com.situ.elder.service.IElderProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ElderProfileServiceImpl implements IElderProfileService {

    private static final Pattern MONTH = Pattern.compile("^\\d{4}-\\d{2}$");

    @Autowired
    private ElderFamilyMapper familyMapper;
    @Autowired
    private ElderChargeMapper chargeMapper;
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private ElderStayMapper stayMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private CareLevelMapper careLevelMapper;
    @Autowired
    private ElderTagMapper familyTagMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private CareTaskMapper taskMapper;

    private Elder requireElder(Long elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new ServiceException("老人不存在");
        }
        return elder;
    }

    // ==================== 家属 ====================

    @Override
    public List<ElderFamily> familyList(Long elderId) {
        requireElder(elderId);
        List<ElderFamily> families = familyMapper.selectList(new LambdaQueryWrapper<ElderFamily>()
                .eq(ElderFamily::getElderId, elderId).orderByDesc(ElderFamily::getId));
        //不把密码带出去
        families.forEach(f -> f.setPassword(null));
        return families;
    }

    @Override
    public void familyAdd(ElderFamily family) {
        if (family == null || family.getElderId() == null) {
            throw new ServiceException("请选择老人");
        }
        requireElder(family.getElderId());
        if (ObjectUtils.isEmpty(family.getName())) {
            throw new ServiceException("请输入家属姓名");
        }
        if (ObjectUtils.isEmpty(family.getRelation())) {
            family.setRelation("家属");
        }
        ensureFamilyNameUnique(family.getName(), null);
        //密码：留空默认 123456，BCrypt 存储
        String raw = (family.getPassword() == null || family.getPassword().isEmpty()) ? "123456" : family.getPassword();
        family.setPassword(com.situ.elder.util.PasswordUtil.encode(raw));
        familyMapper.insert(family);
    }

    @Override
    public void familyUpdate(Long id, ElderFamily family) {
        if (familyMapper.selectById(id) == null) {
            throw new ServiceException("家属记录不存在");
        }
        if (family == null) {
            throw new ServiceException("参数错误");
        }
        if (ObjectUtils.isEmpty(family.getName())) {
            throw new ServiceException("请输入家属姓名");
        }
        ensureFamilyNameUnique(family.getName(), id);
        family.setId(id);
        family.setElderId(null); //不允许通过此接口改所属老人
        //密码：非空则重置为 BCrypt；留空则不修改
        if (family.getPassword() != null && !family.getPassword().isEmpty()) {
            family.setPassword(com.situ.elder.util.PasswordUtil.encode(family.getPassword()));
        } else {
            family.setPassword(null);
        }
        familyMapper.updateById(family);
    }

    @Override
    public void familyDelete(Long id) {
        if (familyMapper.selectById(id) == null) {
            throw new ServiceException("家属记录不存在");
        }
        familyMapper.deleteById(id);
    }

    private void ensureFamilyNameUnique(String name, Long excludeId) {
        Long count = familyMapper.selectCount(new LambdaQueryWrapper<ElderFamily>()
                .eq(ElderFamily::getName, name)
                .ne(excludeId != null, ElderFamily::getId, excludeId));
        if (count > 0) {
            throw new ServiceException("家属姓名「" + name + "」已被占用，家属登录账号需唯一");
        }
    }

    /** 家属登录：校验密码并自动升级历史空密码(默认123456) */
    @Override
    public ElderFamily loginFamily(String name, String password) {
        if (ObjectUtils.isEmpty(name) || ObjectUtils.isEmpty(password)) {
            throw new ServiceException("请输入家属姓名和密码");
        }
        ElderFamily family = familyMapper.selectOne(new LambdaQueryWrapper<ElderFamily>()
                .eq(ElderFamily::getName, name).last("LIMIT 1"));
        if (family == null) {
            throw new ServiceException("家属账号不存在，请联系管理员添加");
        }
        String stored = family.getPassword();
        boolean ok = com.situ.elder.util.PasswordUtil.matches(password, stored)
                || (!com.situ.elder.util.PasswordUtil.isBcrypt(stored)
                && (ObjectUtils.isEmpty(stored) ? "123456".equals(password) : stored.equals(password)));
        if (!ok) {
            throw new ServiceException("密码错误");
        }
        //空/明文密码升级为 BCrypt
        if (!com.situ.elder.util.PasswordUtil.isBcrypt(stored)) {
            ElderFamily upgrade = new ElderFamily();
            upgrade.setId(family.getId());
            upgrade.setPassword(com.situ.elder.util.PasswordUtil.encode(password));
            familyMapper.updateById(upgrade);
        }
        family.setPassword(null);
        return family;
    }

    /** 家属只读视图：老人档案摘要 + 最近护理记录 */
    @Override
    public Map<String, Object> familyElderView(Long elderId) {
        Elder elder = requireElder(elderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("elderId", elder.getId());
        result.put("name", elder.getName());
        result.put("avatar", elder.getAvatar());
        result.put("status", elder.getStatus());
        result.put("birthday", elder.getBirthday());
        result.put("address", elder.getAddress());
        result.put("remark", elder.getRemark());

        //标签
        List<Long> tagIds = familyTagMapper.selectList(new LambdaQueryWrapper<ElderTag>()
                        .eq(ElderTag::getElderId, elderId)).stream().map(ElderTag::getTagId).toList();
        String tagNames = tagIds.isEmpty() ? "" : tagMapper.selectBatchIds(tagIds).stream()
                .map(Tag::getName).collect(java.util.stream.Collectors.joining("、"));
        result.put("tagNames", tagNames);

        //最近护理记录（只读）
        List<Map<String, Object>> tasks = taskMapper.selectList(new LambdaQueryWrapper<CareTask>()
                        .eq(CareTask::getElderId, elderId)
                        .and(w -> w.eq(CareTask::getStatus, 1).or().eq(CareTask::getStatus, 2))
                        .orderByDesc(CareTask::getPlanExecuteDate).last("LIMIT 8"))
                .stream().map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", t.getPlanExecuteDate());
                    m.put("item", t.getCareItemName());
                    m.put("status", taskStatusText(t.getStatus()));
                    m.put("result", t.getExecuteResult());
                    m.put("remark", t.getRemark());
                    return m;
                }).toList();
        result.put("careTasks", tasks);
        return result;
    }

    @Override
    public Map<String, Object> familyElderViewOfFamily(Long familyId) {
        ElderFamily family = familyMapper.selectById(familyId);
        if (family == null) {
            throw new ServiceException("家属账号不存在");
        }
        return familyElderView(family.getElderId());
    }

    private String taskStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        String[] names = {"待执行", "已完成", "已跳过"};
        return status >= 0 && status < names.length ? names[status] : "未知";
    }

    // ==================== 收费项目 ====================

    @Override
    public List<ElderCharge> chargeList(Long elderId, String month) {
        requireElder(elderId);
        return chargeMapper.selectList(new LambdaQueryWrapper<ElderCharge>()
                .eq(ElderCharge::getElderId, elderId)
                .eq(!ObjectUtils.isEmpty(month), ElderCharge::getChargeMonth, month)
                .orderByDesc(ElderCharge::getChargeMonth)
                .orderByDesc(ElderCharge::getId));
    }

    @Override
    public void chargeAdd(ChargeAddRequest request) {
        if (request == null || request.elderId() == null) {
            throw new ServiceException("请选择老人");
        }
        requireElder(request.elderId());
        if (ObjectUtils.isEmpty(request.itemName())) {
            throw new ServiceException("请输入收费项目名称");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("金额必须大于 0");
        }
        int type = request.chargeType() == null ? 0 : request.chargeType();
        if (type != 0 && type != 1) {
            throw new ServiceException("收费类型错误");
        }
        String month = request.chargeMonth();
        if (ObjectUtils.isEmpty(month)) {
            month = java.time.LocalDate.now().toString().substring(0, 7);
        }
        if (!MONTH.matcher(month).matches()) {
            throw new ServiceException("月份格式应为 yyyy-MM");
        }
        ElderCharge charge = new ElderCharge();
        charge.setElderId(request.elderId());
        charge.setItemName(request.itemName());
        charge.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        charge.setChargeType(type);
        charge.setChargeMonth(month);
        charge.setRemark(request.remark());
        chargeMapper.insert(charge);
    }

    @Override
    public void chargeDelete(Long id) {
        if (chargeMapper.selectById(id) == null) {
            throw new ServiceException("收费记录不存在");
        }
        chargeMapper.deleteById(id);
    }

    // ==================== 月度账单 ====================

    @Override
    public Map<String, Object> bill(Long elderId, String month) {
        Elder elder = requireElder(elderId);
        if (ObjectUtils.isEmpty(month)) {
            month = java.time.LocalDate.now().toString().substring(0, 7);
        }
        if (!MONTH.matcher(month).matches()) {
            throw new ServiceException("月份格式应为 yyyy-MM");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", month);
        result.put("elderName", elder.getName());

        //1) 基础月费：当前在住的护理等级 + 床位月费
        ElderStay activeStay = stayMapper.selectOne(new LambdaQueryWrapper<ElderStay>()
                .eq(ElderStay::getElderId, elderId).isNull(ElderStay::getCheckOutDate).last("LIMIT 1"));
        BigDecimal nursingAmount = BigDecimal.ZERO;
        BigDecimal bedAmount = BigDecimal.ZERO;
        String levelName = "未在住";
        String bedInfo = "—";
        if (activeStay != null) {
            if (activeStay.getCareLevelId() != null) {
                CareLevel level = careLevelMapper.selectById(activeStay.getCareLevelId());
                if (level != null) {
                    levelName = level.getName();
                    nursingAmount = level.getPrice() == null ? BigDecimal.ZERO : level.getPrice();
                }
            }
            Bed bed = bedMapper.selectById(activeStay.getBedId());
            if (bed != null) {
                Room room = roomMapper.selectById(bed.getRoomId());
                if (room != null) {
                    bedInfo = room.getFloor() + "楼" + room.getCode() + "/" + bed.getBedNo();
                    bedAmount = room.getPrice() == null ? BigDecimal.ZERO : room.getPrice();
                }
            }
        }
        result.put("nursingLevelName", levelName);
        result.put("nursingAmount", nursingAmount);
        result.put("bedInfo", bedInfo);
        result.put("bedAmount", bedAmount);

        //2) 该月收费项：一次性(charge_month=本月) + 月度(charge_month<=本月，每月重复)
        final String targetMonth = month;
        List<ElderCharge> charges = chargeMapper.selectList(new LambdaQueryWrapper<ElderCharge>()
                .eq(ElderCharge::getElderId, elderId)
                .and(w -> w.eq(ElderCharge::getChargeType, 0).eq(ElderCharge::getChargeMonth, targetMonth)
                        .or().eq(ElderCharge::getChargeType, 1).le(ElderCharge::getChargeMonth, targetMonth))
                .orderByDesc(ElderCharge::getChargeType)
                .orderByDesc(ElderCharge::getId));
        result.put("items", charges);

        BigDecimal extras = charges.stream()
                .map(c -> c.getAmount() == null ? BigDecimal.ZERO : c.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = nursingAmount.add(bedAmount).add(extras).setScale(2, RoundingMode.HALF_UP);
        result.put("extraAmount", extras.setScale(2, RoundingMode.HALF_UP));
        result.put("totalAmount", total);
        return result;
    }
}
