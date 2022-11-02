package com.zheng.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zheng.reggie.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

}
