package com.situ.elder.controller.admin;

import com.situ.elder.pojo.dto.BedAddRequest;
import com.situ.elder.pojo.dto.BedStatusRequest;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.service.IRoomService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 床位管理
 */
@RestController
@RequestMapping("/admin/beds")
public class BedController {

    @Autowired
    private IRoomService roomService;

    /** 某个房间的床位 */
    @GetMapping("/room/{roomId}")
    public Result<List<Bed>> byRoom(@PathVariable Long roomId) {
        return Result.ok(roomService.bedsOfRoom(roomId));
    }

    /** 空闲床位（办理入住用） */
    @GetMapping("/free")
    public Result<List<Bed>> free() {
        return Result.ok(roomService.freeBeds());
    }

    /** 批量新增床位 */
    @PostMapping
    public Result add(@RequestBody BedAddRequest request) {
        roomService.addBeds(request);
        return Result.ok("新增成功");
    }

    /** 状态：空闲<->维修 */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody BedStatusRequest request) {
        roomService.updateBedStatus(id, request == null ? null : request.status());
        return Result.ok("操作成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        roomService.deleteBed(id);
        return Result.ok("删除成功");
    }
}
