package com.zheng.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.reggie.dto.SetmealDto;
import com.zheng.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {


    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    void saveWithDishes(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐与菜品的关联数据
     *
     * @param ids
     */
    void removeWithDishes(List<Long> ids);
}
