package com.zheng.reggie.dto;

import com.zheng.reggie.entity.OrderDetail;
import com.zheng.reggie.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {

    private List<OrderDetail> orderDetails;
}
