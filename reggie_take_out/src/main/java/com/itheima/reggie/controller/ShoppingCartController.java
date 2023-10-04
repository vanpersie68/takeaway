package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController
{
    @Autowired
    private ShoppingCartService shoppingCartService;

    //添加购物车
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart)
    {
        log.info("购物车数据：{}", shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者套餐是否已经在购物车当中(存在，则在原来的基础上加一;不存在，默认为1)
        Long dishId = shoppingCart.getDishId();
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",currentId);

        if(dishId != null)
        {
            //添加到购物车的为菜品
            queryWrapper.eq("dish_id",dishId);
        }
        else
        {
            //添加到购物车的为套餐
            queryWrapper.eq("setmeal_id",shoppingCart.getSetmealId());
        }

        //SQL:select *from shopping_cart where user_id=? and dish_id/setmeal_id =?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null)
        {
            //如果已经存在，则在原来的基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }
        else
        {
            //如果不存在，则添加到购物车中，默认为一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    //查看购物车
    @GetMapping("/list")
    public R<List<ShoppingCart>> list()
    {
        log.info("查看购物车");
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",BaseContext.getCurrentId());
        queryWrapper.orderByDesc("create_time");
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    //清空购物车
    @DeleteMapping("/clean")
    public R<String> clean()
    {
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    //减少菜品
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart)
    {
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",BaseContext.getCurrentId());

        if(dishId != null)
        {
            queryWrapper.eq("dish_id",dishId);
        }
        else
        {
            queryWrapper.eq("setmeal_id",setmealId);
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        Integer number = cartServiceOne.getNumber();

        if(number > 1)
        {

            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }
        else if(number == 1)
        {
            shoppingCartService.remove(queryWrapper);
        }

        return R.success(cartServiceOne);
    }
}
