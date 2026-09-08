package com.situ.elder.pojo.query;

import lombok.Data;

/**
 * 房间查询
 */
@Data
public class RoomQuery {

    private Integer page = 1;
    private Integer limit = 10;

    /** 房间号关键字 */
    private String keyword;

    private Integer floor;

    private Integer status;
}
