package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ElderTagVO {
    private List<Tag> tagList;
    private List<Long> assignedTagIdList;
}
