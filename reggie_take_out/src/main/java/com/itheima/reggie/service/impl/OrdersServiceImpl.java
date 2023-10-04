package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService
{
    @Autowired
    private ShoppingCartService shoppingcartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    //用户下单
    @Override
    @Transactional //多次操控数据库，增加事务控制注解
    public void submit(Orders orders)
    {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        QueryWrapper<ShoppingCart> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<ShoppingCart> shoppingCartLists = shoppingcartService.list(queryWrapper);

        if(shoppingCartLists == null || shoppingCartLists.size() == 0)
        {
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);
        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook  = addressBookService.getById(addressBookId);
        if(addressBook == null)
        {
            throw new CustomException("地址有误，不能下单");
        }


        long orderId = IdWorker.getId(); //订单号

        //计算总金额
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCartList : shoppingCartLists)
        {
            OrderDetail orderDetail = new OrderDetail();
            //将所有订单物品进行封装，为了后续方便向订单明细表(OrderDetail)添加数据
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCartList.getNumber());
            orderDetail.setDishFlavor(shoppingCartList.getDishFlavor());
            orderDetail.setDishId(shoppingCartList.getDishId());
            orderDetail.setSetmealId(shoppingCartList.getSetmealId());
            orderDetail.setName(shoppingCartList.getName());
            orderDetail.setImage(shoppingCartList.getImage());
            orderDetail.setAmount(shoppingCartList.getAmount());
            amount.addAndGet(shoppingCartList.getAmount().multiply(
                    new BigDecimal(shoppingCartList.getNumber())).intValue());
            orderDetailList.add(orderDetail);
        }

        //向订单表(order)中插入一条数据
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2); //订单状态，2：待派送
        orders.setAmount(new BigDecimal(amount.get()));//计算总金额
        orders.setUserId(userId);
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()==null ? "" : addressBook.getProvinceName())
                +(addressBook.getCityName()==null ? "" : addressBook.getCityName())
                +(addressBook.getDistrictName()==null ? "" : addressBook.getDistrictName())
                +(addressBook.getDetail()==null ? "" : addressBook.getDetail()));

        this.save(orders);
        //向订单明细表(OrderDetail)中插入多条数据
        orderDetailService.saveBatch(orderDetailList);
        //清空购物车数据
        shoppingcartService.remove(queryWrapper);
    }
}
