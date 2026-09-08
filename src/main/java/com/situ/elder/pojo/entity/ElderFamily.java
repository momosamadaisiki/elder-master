package com.situ.elder.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 老人家属
 */
@Data
@TableName("elder_family")
public class ElderFamily {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long elderId;

    /** 家属姓名 */
    private String name;

    /** 关系 */
    private String relation;

    private String phone;

    /** 登录密码(BCrypt)，留空默认 123456；列表输出时置空 */
    private String password;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
