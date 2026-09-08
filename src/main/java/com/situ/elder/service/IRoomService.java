package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.dto.BedAddRequest;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;

import java.util.List;

/**
 * 房间/床位服务
 */
public interface IRoomService {

    IPage<Room> pageRooms(RoomQuery query);

    void addRoom(Room room);

    void updateRoom(Room room);

    /** 删除房间（房间内不能有床位） */
    void deleteRoom(Long id);

    List<Bed> bedsOfRoom(Long roomId);

    /** 批量新增床位（校验重复与容量） */
    void addBeds(BedAddRequest request);

    /** 状态流转：空闲<->维修（占用中不可改） */
    void updateBedStatus(Long bedId, Integer status);

    /** 删除床位（仅空闲） */
    void deleteBed(Long bedId);

    /** 空闲床位（含房间信息，供办理入住选择） */
    List<Bed> freeBeds();
}
