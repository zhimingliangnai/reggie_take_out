package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.common.exception.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.pojo.Dish;
import com.itheima.reggie.pojo.DishFlavor;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService flavorService;

    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品到数据库中
        this.save(dishDto);

        /*
         * 保存在集合中的数据没有封装菜品ID这个重要数据，需要手动添加
         */
        // 执行玩保存后，数据以封装进dishDto中，可以直接获取id
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 保存口味到数据库中
        flavorService.saveBatch(dishDto.getFlavors());
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 根据id查询菜品数据
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 获取dishId
        Long dishId = dish.getId();
        // 构建条件构造器
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dishId);

        // 根据dishId查询口味
        List<DishFlavor> flavors = flavorService.list(wrapper);
        // 设置flavors
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 修改菜品
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 修改dish表
        // 获取dishId
        Long dishId = dishDto.getId();
        // 通过id修改菜品
        this.updateById(dishDto);

        // 修改dish_flavor表
        // 构建条件构造器
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId,dishId);
        // 清除当前的口味数据
        flavorService.remove(wrapper);

        // 获取flavors
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 添加新增的口味数据
        flavorService.saveBatch(flavors);

    }

    @Override
    public void deleteWithFlavor(@RequestParam Long[] ids) {

        // 构建条件构造器
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getStatus,1);
        dishWrapper.in(Dish::getId, Arrays.asList(ids));
        // 查询菜品状态是否启用
        int count = this.count(dishWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在销售中，不能删除");
        }

        // 根据Id删除菜品数据
        this.removeByIds(Arrays.asList(ids));

        // 根据dishId删除口味数据
        // 构建条件构造器
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.in(DishFlavor::getDishId, Arrays.asList(ids));

        flavorService.remove(dishFlavorWrapper);
    }

}
