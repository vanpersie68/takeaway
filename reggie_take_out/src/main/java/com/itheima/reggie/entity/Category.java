package com.itheima.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类
 */
@Data
public class Category implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer type;   //类型 1 菜品分类 2 套餐分类
    private String name;    //分类名称
    private Integer sort;   //顺序
    @TableField(fill = FieldFill.INSERT)    //创建时间
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) //更新时间
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)    //创建人
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE) //修改人
    private Long updateUser;
    //private Integer isDeleted;    //是否删除
}
