package com.zheng.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     * @return
     */
    void submit(Orders orders);
}
