package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServicelmpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService
{
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /*根据id来删除分类，在删除前需要进行判断，判断当前分类是否关联了对应的菜品或套餐，
    * 如果关联了，则返回删除失败*/
    @Override
    public void remove(Long id)
    {
        //查询当前分类是否关联了菜品，如果关联，直接抛出一个异常
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        dishQueryWrapper.eq("category_id",id);
        int dishCount = dishService.count(dishQueryWrapper);

        if(dishCount > 0)
        {
            //已经关联菜品，抛出业务异常
            throw new CustomException("已经关联菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果关联，直接抛出一个异常
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.eq("category_id",id);
        int setmealCount = setmealService.count(setmealQueryWrapper);
        if(setmealCount > 0)
        {
            //已经关联套餐，抛出业务异常
            throw new CustomException("已经关联套餐，不能删除");
        }

        //正常删除分类
        super.removeById(id);
    }
}
