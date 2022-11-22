package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.exception.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.pojo.Setmeal;
import com.itheima.reggie.pojo.SetmealDish;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {
    @Autowired
    private SetMealDishService setMealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐信息，操作setmeal表，进行insert操作
        this.save(setmealDto);

        // 获取关系信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 处理信息，添加SetmealId
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存套餐菜品关系信息，操作setmeal_dish表，进行insert操作
        setMealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void deleteWithMealDish(Long[] ids) {
        // 通过id查询Setmeal表，判断是否处于起售状态
        // 构建条件构造器
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getStatus, 1);
        wrapper.in(Setmeal::getId, Arrays.asList(ids));

        int count = this.count(wrapper);
        if (count > 0) {
            throw new CustomException("套餐正在销售中，不能删除");
        }

        // 执行mapper方法
        this.removeByIds(Arrays.asList(ids));

        // 构建条件构造器
        LambdaQueryWrapper<SetmealDish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.in(SetmealDish::getSetmealId,Arrays.asList(ids));
        setMealDishService.remove(dishWrapper);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 通过id查询setmeal表
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 拷贝setmeal至setmealDish中
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 通过setmealId查询setmeal_dish表
        // 构建条件构造器
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setMealDishService.list(wrapper);
        // 设置list
        if (list != null) {
            setmealDto.setSetmealDishes(list);
        }

        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto dto) {
        Long setmealId = dto.getId();
        this.updateById(dto);

        // 先根据setmealId删除菜品关系数据
        List<SetmealDish> dishes = dto.getSetmealDishes();
        // 构建条件构造器
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,setmealId);

        setMealDishService.remove(wrapper);

        dishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        setMealDishService.saveBatch(dishes);
    }
}
