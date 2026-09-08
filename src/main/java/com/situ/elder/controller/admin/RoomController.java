package com.situ.elder.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.service.IRoomService;
import com.situ.elder.util.JwtUtil;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 房间管理
 */
@RestController
@RequestMapping("/admin/rooms")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @GetMapping
    public Result<IPage<Room>> list(RoomQuery query) {
        return Result.ok(roomService.pageRooms(query));
    }

    @PostMapping
    public Result add(@RequestBody Room room) {
        roomService.addRoom(room);
        return Result.ok("新增成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        roomService.updateRoom(room);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.ok("删除成功");
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
