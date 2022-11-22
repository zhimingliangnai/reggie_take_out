package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.pojo.Category;
import com.itheima.reggie.pojo.Dish;
import com.itheima.reggie.pojo.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;



    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return R.success("添加成功");
    }


    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    /*
        查两张表
        dish，category。
        dish表全部信息，category名称
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构建分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dtoPage = new Page<>();

        // 构建条件构造器
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!Strings.isEmpty(name),Dish::getName,name);
        wrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, wrapper);

        // 将除了records以外的所有属性拷贝至dtoPage中
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        // 获取records

        List<Dish> records = pageInfo.getRecords();
        // 处理records
        List<DishDto> lists = records.stream().map((item) -> {
            DishDto dto = new DishDto();
            // 将属性拷贝至dto
            BeanUtils.copyProperties(item,dto);
            // 获取categoryId
            Long categoryId = item.getCategoryId();
            // 通过id查询category，获取name
            String categoryName = categoryService.getById(categoryId).getName();
            // 将name添加进dto中
            dto.setCategoryName(categoryName);

            return dto;
        }).collect(Collectors.toList());
        // 将list添加进dtoPage中
        dtoPage.setRecords(lists);

        return R.success(dtoPage);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> queryDishById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("添加成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteById(Long[] ids) {
        log.info("执行删除方法");
        dishService.deleteWithFlavor(ids);

        return R.success("删除成功");

    }

    /**
     * 修改方法
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable Integer status, Long[] ids) {
        log.info("status = {},ids = {}", status, ids);
        List<Dish> lists = new ArrayList<>();

        // 处理ids
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            lists.add(dish);
        }
        // 执行mapper
        boolean res = dishService.updateBatchById(lists);

        if (res) {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    @GetMapping("/list")
    public R<List<DishDto>> queryDishList(Dish dish) {

        // 构建条件构造器
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        wrapper.eq(Dish::getStatus, 1);
        // 根据id查询菜品
        List<Dish> list = dishService.list(wrapper);

        LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();

        List<DishDto> dtos = list.stream().map((item) -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(item, dto);

            Long categoryId = item.getCategoryId();

            // 通过categoryId查询分类
            Category category = categoryService.getById(categoryId);

            // 设置分类名称
            if (category != null) {
                dto.setCategoryName(category.getName());
            }

            // 查询当前菜品口味
            Long dishId = item.getId();
            flavorWrapper.clear();
            flavorWrapper.eq(DishFlavor::getDishId, dishId);

            List<DishFlavor> flavors = dishFlavorService.list(flavorWrapper);

            if (flavors != null) {
                dto.setFlavors(flavors);
            }
            return dto;
        }).collect(Collectors.toList());


        if (list != null) {
            return R.success(dtos);
        }
        return R.error("未查询到该结果，请稍后再试。");
    }
}
