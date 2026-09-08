package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.BedMapper;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.pojo.dto.BedAddRequest;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    public static final int BED_FREE = 0;
    public static final int BED_OCCUPIED = 1;
    public static final int BED_MAINTENANCE = 2;

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private BedMapper bedMapper;

    @Override
    public IPage<Room> pageRooms(RoomQuery query) {
        IPage<Room> page = new Page<>(query.getPage(), query.getLimit());
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(!ObjectUtils.isEmpty(query.getKeyword()), w -> w
                        .like(Room::getCode, query.getKeyword())
                        .or().like(Room::getRemark, query.getKeyword()))
                .eq(!ObjectUtils.isEmpty(query.getFloor()), Room::getFloor, query.getFloor())
                .eq(!ObjectUtils.isEmpty(query.getStatus()), Room::getStatus, query.getStatus())
                .orderByAsc(Room::getFloor).orderByAsc(Room::getCode);
        IPage<Room> result = roomMapper.selectPage(page, wrapper);
        attachBedStats(result.getRecords());
        return result;
    }

    /** 给房间列表填 bedTotal / occupied */
    private void attachBedStats(List<Room> rooms) {
        if (rooms.isEmpty()) {
            return;
        }
        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        List<Bed> beds = bedMapper.selectList(new LambdaQueryWrapper<Bed>().in(Bed::getRoomId, roomIds));
        Map<Long, List<Bed>> byRoom = beds.stream().collect(Collectors.groupingBy(Bed::getRoomId));
        rooms.forEach(room -> {
            List<Bed> list = byRoom.getOrDefault(room.getId(), List.of());
            room.setBedTotal(list.size());
            room.setOccupied((int) list.stream().filter(b -> b.getStatus() != null && b.getStatus() == BED_OCCUPIED).count());
        });
    }

    @Override
    public void addRoom(Room room) {
        validateRoom(room);
        if (roomMapper.selectCount(new LambdaQueryWrapper<Room>().eq(Room::getCode, room.getCode())) > 0) {
            throw new ServiceException("房间号已存在");
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        if (room.getCapacity() == null || room.getCapacity() < 1) {
            room.setCapacity(1);
        }
        if (room.getPrice() == null) {
            room.setPrice(java.math.BigDecimal.ZERO);
        }
        roomMapper.insert(room);
    }

    @Override
    public void updateRoom(Room room) {
        if (room.getId() == null || roomMapper.selectById(room.getId()) == null) {
            throw new ServiceException("房间不存在");
        }
        validateRoom(room);
        roomMapper.updateById(room);
    }

    private void validateRoom(Room room) {
        if (ObjectUtils.isEmpty(room.getCode())) {
            throw new ServiceException("请输入房间号");
        }
        if (room.getFloor() == null) {
            throw new ServiceException("请选择楼层");
        }
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new ServiceException("房间不存在");
        }
        if (bedMapper.selectCount(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, id)) > 0) {
            throw new ServiceException("该房间下还有床位，请先删除床位");
        }
        roomMapper.deleteById(id);
    }

    @Override
    public List<Bed> bedsOfRoom(Long roomId) {
        if (roomMapper.selectById(roomId) == null) {
            throw new ServiceException("房间不存在");
        }
        return bedMapper.selectList(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getRoomId, roomId).orderByAsc(Bed::getBedNo));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBeds(BedAddRequest request) {
        if (request == null || request.roomId() == null) {
            throw new ServiceException("参数错误");
        }
        Room room = roomMapper.selectById(request.roomId());
        if (room == null) {
            throw new ServiceException("房间不存在");
        }
        List<String> nos = request.bedNos() == null ? new ArrayList<>()
                : request.bedNos().stream().map(String::trim).filter(n -> !n.isEmpty()).distinct().toList();
        if (nos.isEmpty()) {
            throw new ServiceException("请至少输入一个床号");
        }
        List<Bed> existed = bedMapper.selectList(new LambdaQueryWrapper<Bed>().eq(Bed::getRoomId, room.getId()));
        if (existed.size() + nos.size() > room.getCapacity()) {
            throw new ServiceException("超出房间容量（当前 " + room.getCapacity() + " 床）");
        }
        for (String no : nos) {
            if (existed.stream().anyMatch(b -> b.getBedNo().equals(no))) {
                throw new ServiceException("床号已存在：" + no);
            }
        }
        nos.forEach(no -> {
            Bed bed = new Bed();
            bed.setRoomId(room.getId());
            bed.setBedNo(no);
            bed.setStatus(BED_FREE);
            bedMapper.insert(bed);
        });
    }

    @Override
    public void updateBedStatus(Long bedId, Integer status) {
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        if (bed.getStatus() == BED_OCCUPIED) {
            throw new ServiceException("床位正在使用中，不可修改");
        }
        if (status == null || (status != BED_FREE && status != BED_MAINTENANCE)) {
            throw new ServiceException("状态只支持：空闲/维修");
        }
        bed.setStatus(status);
        bedMapper.updateById(bed);
    }

    @Override
    public void deleteBed(Long bedId) {
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            throw new ServiceException("床位不存在");
        }
        if (bed.getStatus() == BED_OCCUPIED) {
            throw new ServiceException("床位正在使用中，不可删除");
        }
        bedMapper.deleteById(bedId);
    }

    @Override
    public List<Bed> freeBeds() {
        List<Bed> beds = bedMapper.selectList(new LambdaQueryWrapper<Bed>()
                .eq(Bed::getStatus, BED_FREE).orderByAsc(Bed::getRoomId).orderByAsc(Bed::getBedNo));
        attachRoomInfo(beds);
        return beds;
    }

    private void attachRoomInfo(List<Bed> beds) {
        if (beds.isEmpty()) {
            return;
        }
        List<Long> roomIds = beds.stream().map(Bed::getRoomId).distinct().toList();
        Map<Long, Room> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, r -> r));
        beds.forEach(bed -> {
            Room room = roomMap.get(bed.getRoomId());
            if (room != null) {
                bed.setRoomCode(room.getCode());
                bed.setFloor(room.getFloor());
            }
        });
    }
}
