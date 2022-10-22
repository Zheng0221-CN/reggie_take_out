package com.zheng.reggie.dto;

import com.zheng.reggie.entity.Setmeal;
import com.zheng.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
