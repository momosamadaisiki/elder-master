package com.situ.elder.controller.admin;

import com.situ.elder.pojo.entity.ElderFamily;
import com.situ.elder.service.IElderProfileService;
import com.situ.elder.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 老人家属管理
 */
@RestController
@RequestMapping("/admin/elder-families")
public class ElderFamilyController {

    @Autowired
    private IElderProfileService elderProfileService;

    @GetMapping
    public Result<List<ElderFamily>> list(@RequestParam("elderId") Long elderId) {
        return Result.ok(elderProfileService.familyList(elderId));
    }

    @PostMapping
    public Result add(@RequestBody ElderFamily family) {
        elderProfileService.familyAdd(family);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ElderFamily family) {
        elderProfileService.familyUpdate(id, family);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        elderProfileService.familyDelete(id);
        return Result.ok("删除成功");
    }
}
