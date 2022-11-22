package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.exception.CustomException;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.pojo.Category;
import com.itheima.reggie.pojo.Dish;
import com.itheima.reggie.pojo.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;

    @Override
    public void remove(Long id) {
        // 添加查询条件，根据id查询
        LambdaQueryWrapper<Setmeal> setMealWrapper = new LambdaQueryWrapper<>();
        setMealWrapper.eq(Setmeal::getCategoryId, id);
        // 查询当前套餐是否已有关联，已有关联抛出异常
        int setMealCount = setMealService.count(setMealWrapper);

        if (setMealCount > 0) {
            // 已有关联，抛出异常
            throw new CustomException("当前分类以关联了套餐，无法删除");
        }

        // 添加查询数，根据id查询
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId,id);

        // 查询当前菜品是否已有关联，已有关联抛出异常
        int dishCount = dishService.count(dishWrapper);
        if (dishCount > 0 ) {
            // 已有关联，抛出异常
            throw new CustomException("当前分类以关联了菜品，无法删除");

        }

        // 执行删除方法
        super.removeById(id);

    }
}
