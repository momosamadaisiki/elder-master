package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.Permission;
import lombok.Data;

import java.util.List;

@Data
public class PermissionVO extends Permission {
    private List<PermissionVO> children;
}
