package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService cartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart cart) {
        log.info(cart.toString());
        // 获取当前用户id
        Long userId = BaseContext.getId();
        cart.setUserId(userId);
        Long dishId = cart.getDishId();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        if (dishId != null) {
            // 当前添加的是菜品
            // 如果存在number+1
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, cart.getSetmealId());
        }
        // 查询当前菜品或套餐是否已存在
        ShoppingCart shoppingCart = cartService.getOne(wrapper);

        if (shoppingCart == null ) {
            // 菜品不存在
            cart.setNumber(1);
            cartService.save(cart);
        } else {
            // 菜品存在
            Integer number = shoppingCart.getNumber();
            shoppingCart.setCreateTime(LocalDateTime.now());
            Long cardId = shoppingCart.getId();
            cart.setId(cardId);
            cart.setNumber(number + 1);
                cartService.updateById(cart);
            shoppingCart = cart;
        }
        return R.success(shoppingCart);
    }

    /**
     * 查询购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list () {
        // 获取用户id
        Long userId = BaseContext.getId();
        // 设置查询条件
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        wrapper.orderByAsc(ShoppingCart::getCreateTime);

        return R.success(cartService.list());
    }

    /**
     * 减少菜品
     * @param map
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody Map<String, Long> map) {
        log.info(map.toString());
        Long dishId = map.get("dishId");

        // 构建条件构造器
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getId());
        // 判断是否是菜品
        if (dishId != null) {
            // 是菜品
            // 设置菜品id为条件
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 是套餐
            // 设置套餐id为条件
            wrapper.eq(ShoppingCart::getSetmealId, map.get("setmealId"));
        }

        ShoppingCart cart = cartService.getOne(wrapper);

        Integer number = cart.getNumber();
        // 判断number是否为1
        if (number == 1) {
            // 删除数据
            // 执行mapper
            cartService.remove(wrapper);
        } else {
            // 执行修改操作，设置number-1
            cart.setNumber(number - 1);
            cartService.updateById(cart);
        }

        return R.success("移除成功");
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        // 根据userId删除数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getId());

        boolean res = cartService.remove(wrapper);

        if (res) {
            return R.success("清空购物车成功");
        }

        return R.error("清空购物车失败");
    }

}
