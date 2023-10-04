package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal>
{
    //新增套餐，同时要保持与菜品的关联关系
    void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐和菜品的关联数据
    void removeWithDish(List<Long> ids);

    //根据id来查询套餐信息和菜品信息(在修改套餐信息的时候实现套餐信息回显的步骤)
    SetmealDto getByIdWithDish(Long id);

    //修改套餐,同时更改套餐对应的菜品
    void updateWithDish(SetmealDto setmealDto);
}
