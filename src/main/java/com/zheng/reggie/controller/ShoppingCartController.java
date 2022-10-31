package com.zheng.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.reggie.common.BaseContext;
import com.zheng.reggie.common.R;
import com.zheng.reggie.entity.ShoppingCart;
import com.zheng.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {

        // 设置用户 id ，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (shoppingCart.getDishId() != null) {

            // 添加到购物车的是菜品
            Long dishId = shoppingCart.getDishId();
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {

            // 添加到购物车的是套餐
            Long setmealId = shoppingCart.getSetmealId();
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if (cartServiceOne != null) {

            // 如果已经存在，就在原来数量基础上 + 1
            cartServiceOne.setNumber(cartServiceOne.getNumber() + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {

            // 如果不存在，则添加到购物车，数量默认为 1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }


        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCarts);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart cart) {
        log.info(cart.toString());

        // 通过用户 id 和菜品 id 或套餐 id，查找要减少数量的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        if (cart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, cart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }
        ShoppingCart shoppingCart = shoppingCartService.getOne(queryWrapper);

        // 得到对应主键
        Long id = shoppingCart.getId();

        // 如果数量为 1 ，则直接删除这条购物车数据，数量大于 1 ，则更新
        if (shoppingCart.getNumber() == 1) {
            shoppingCartService.removeById(id);
        } else {
            shoppingCartService.updateById(shoppingCart);
        }

        shoppingCart.setNumber(shoppingCart.getNumber() - 1);
        return R.success(shoppingCart);
    }
}
