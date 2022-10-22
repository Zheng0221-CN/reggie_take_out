package com.zheng.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
