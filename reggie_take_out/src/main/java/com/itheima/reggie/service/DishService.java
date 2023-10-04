package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish>
{
    //新增菜品，通过插入菜品和对应的口味数据。需要操作两张表，dish 和 dish_flavor
    void saveWithFlavor(DishDto dishDto);

    //删除菜品，同时删除菜品和口味的关联数据
    void removeWithFlavor(List<Long> ids);

    //根据id查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新口味信息
    void updateWithFlavor(DishDto dishDto);


}
