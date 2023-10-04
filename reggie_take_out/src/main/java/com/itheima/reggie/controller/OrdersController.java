package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;

import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController
{
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    //用户下单
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders)
    {
        log.info("订单数据:{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    //前台的订单信息分页查询
    @GetMapping("/userPage")
    @Transactional
    public R<Page> pageFront(int page, int pageSize)
    {
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", BaseContext.getCurrentId());
        queryWrapper.orderByDesc("order_time");

        ordersService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders record : records)
        {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record,ordersDto);

            Long orderId = record.getId();
            QueryWrapper<OrderDetail> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("order_id",orderId);
            int count = orderDetailService.count(queryWrapper1);
            ordersDto.setSumNum(count);
            ordersDtoList.add(ordersDto);
            System.err.println(count);
        }
        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

    //后台的订单信息分页查询
    @GetMapping("/page")
    public R<Page> pageBackend(int page, int pageSize, String number)
    {
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(number),"number",number);
        queryWrapper.orderByDesc("order_time");
        ordersService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = new ArrayList<>();

        for (Orders record : records)
        {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record,ordersDto);
            String name = "用户" + record.getUserId();
            ordersDto.setUserName(name);
            ordersDtoList.add(ordersDto);
        }

        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    //改变订单的配送状态
    @PutMapping
    public R<String> changeStatus(@RequestBody Orders orders)
    {
        Integer status = orders.getStatus();
        Long orderId = orders.getId();
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",orderId);
        Orders order = ordersService.getOne(queryWrapper);
        order.setStatus(status);
        ordersService.updateById(order);
        return R.success("派送成功");
    }

    //再来一单
    @Transactional
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders)
    {
        Long orderId = orders.getId();
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);

        List<OrderDetail> orderDetailLists = orderDetailService.list(queryWrapper);

        for (OrderDetail orderDetailList : orderDetailLists)
        {
            long shoppingCartId = IdWorker.getId();
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setId(shoppingCartId);
            shoppingCart.setName(orderDetailList.getName());
            shoppingCart.setImage(orderDetailList.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setDishId(orderDetailList.getDishId());
            shoppingCart.setSetmealId(orderDetailList.getSetmealId());
            shoppingCart.setDishFlavor(orderDetailList.getDishFlavor());
            shoppingCart.setNumber(orderDetailList.getNumber());
            shoppingCart.setAmount(orderDetailList.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success("再来一单");
    }
}
