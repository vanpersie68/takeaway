package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService
{
    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，通过插入菜品和对应的口味数据。需要操作两张表，dish 和 dish_flavor
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto)
    {
        //保存菜品基本信息到菜品表dish
        this.save(dishDto);

        //获得菜品的id
        Long dishId = dishDto.getId();

        //将菜品的id 赋值给菜品口味中的菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors)
        {
            flavor.setDishId(dishId);
        }

        //保存菜品口味到菜品数据表dish_flavor
        dishFlavorService.saveBatch(flavors); //批量保存
    }

    //删除 和 批量删除 菜品 (因为是多个id，所以使用容器)
    @Override
    public void removeWithFlavor(List<Long> ids)
    {
        for(Long id : ids)
        {
            Dish dish = this.getById(id);
            //查询菜品的状态，确定是否可以删除。
            if(dish.getStatus() == 0)
            {
                //如果可以删除，先删除套餐表中的数据 dish
                this.removeById(id);
                //删除关系表中的数据----dish_flavor
                QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("dish_id",id);
                dishFlavorService.remove(queryWrapper);
            }
            else
            {
                throw new CustomException("菜品正在售卖中，不能删除");
            }
        }
    }

    //根据id查询菜品信息和对应的口味信息
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id)
    {
        //查询菜品的基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品口味信息
        QueryWrapper<DishFlavor> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("dish_id",dish.getId());
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(list);
        return dishDto;
    }

    //更新菜品信息，同时更新口味信息
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto)
    {
        //更新dish表基本信息
        this.updateById(dishDto);

        //更新dish_flavor表信息delete操作 (清除当前菜品对应口味数据)
        QueryWrapper<DishFlavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dish_id", dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //更新dish_flavor表信息insert操作 (添加当前菜品对应口味数据)
        List<DishFlavor> flavors = dishDto.getFlavors(); //为新的口味 设置对应的菜品id

        for(DishFlavor flavor : flavors)
        {
            flavor.setDishId(dishDto.getId());
        }

        dishFlavorService.saveBatch(flavors);
    }


}
