package com.zheng.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zheng.reggie.common.BaseContext;
import com.zheng.reggie.common.R;
import com.zheng.reggie.dto.OrderDto;
import com.zheng.reggie.entity.OrderDetail;
import com.zheng.reggie.entity.Orders;
import com.zheng.reggie.service.OrderDetailService;
import com.zheng.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 订单信息分页查询
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) throws ParseException {

        // 构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 添加筛选条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        // 订单号查询条件
        if (number != null) {
            long idNumber = Long.parseLong(number);
            queryWrapper.like(Orders::getId, idNumber);

        }

        // 开始日期条件
        if (beginTime != null) {
            Date begin = simpleDateFormat.parse(beginTime);
            queryWrapper.gt(Orders::getOrderTime, begin);
        }

        // 结束日期条件
        if (endTime != null) {
            Date end = simpleDateFormat.parse(endTime);
            queryWrapper.lt(Orders::getOrderTime, end);
        }

        // 排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        ordersService.page(pageInfo, queryWrapper);

        // 设置订单号和用户
        List<Orders> records = pageInfo.getRecords();

        // 通过 peek() 为每个元素提供消费函数
        records = records.stream().peek((item) -> {
            item.setNumber(item.getId().toString());
            item.setUserName(item.getUserId().toString());
        }).collect(Collectors.toList());
        pageInfo.setRecords(records);

        return R.success(pageInfo);
    }

    /**
     * 更改订单状态
     *
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> status(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return R.success("订单状态更改成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {

        // 先得到 Orders 的 Page 对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);

        // 创建 OrderDto 的 Page 对象
        Page<OrderDto> dtoPage = new Page<>(page, pageSize);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Orders> records = pageInfo.getRecords();

        List<OrderDto> orderDtoList = records.stream().map(item -> {

            // 得到订单号
            Long orderId = item.getId();

            // 根据订单号，得到对应 orderDetail 的集合
            LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            orderDetailService.list(orderDetailQueryWrapper);
            List<OrderDetail> orderDetails = orderDetailService.list(orderDetailQueryWrapper);

            // 处理 OrderDto 对象
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item, orderDto);
            orderDto.setOrderDetails(orderDetails);
            return orderDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(orderDtoList);

        return R.success(dtoPage);
    }
}
