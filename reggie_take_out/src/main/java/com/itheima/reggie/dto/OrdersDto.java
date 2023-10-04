package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Orders;
import lombok.Data;

@Data
public class OrdersDto extends Orders
{
    int sumNum;
    String userName;
}