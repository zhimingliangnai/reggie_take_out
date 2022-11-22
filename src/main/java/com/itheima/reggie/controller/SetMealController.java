package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.pojo.Setmeal;
import com.itheima.reggie.pojo.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private SetMealDishService dishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        setMealService.saveWithDish(setmealDto);
        return R.success("套餐添加成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}",page, pageSize, name);
        // 构建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // 构建dto分页构造器
        Page<SetmealDto> dtoPage = new Page<>();

        // 构建条件构造器
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Setmeal::getName, name);
        wrapper.orderByDesc(Setmeal::getUpdateTime);
        // 执行mapper方法
        setMealService.page(pageInfo, wrapper);

        // 拷贝pageInfo中除了records的所有属性
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        // 获取records进行处理
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> lists = records.stream().map((item) -> {
            SetmealDto dto = new SetmealDto();
            // 将item中所有属性拷贝至dto中
            BeanUtils.copyProperties(item, dto);
            // 获取categoryId
            Long categoryId = item.getCategoryId();

            // 根据categoryId查询Setmeal_dish表，获取套餐名称
            String categoryName = categoryService.getById(categoryId).getName();
            // 设置套餐名称
            dto.setCategoryName(categoryName);
            return dto;
        }).collect(Collectors.toList());

        // 设置records
        dtoPage.setRecords(lists);

        return R.success(dtoPage);
    }

    /**
     * 修改套餐状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status,Long[] ids) {
        log.info("status = {}, ids = {}",status, ids);
        // 创建Setmeal集合
        List<Setmeal> setmeals = new ArrayList<>();

        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmeals.add(setmeal);
        }
        // 执行update方法
        boolean res = setMealService.updateBatchById(setmeals);

        if (res) {
            return R.success("修改状态成功");
        }

        return R.error("修改状态失败");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long[] ids) {
        log.info("ids = {}", ids);
        /*
            先判断套餐是否处于起售状态，若处于起售状态，则返回警告
            若处于停售状态，则进行删除操作。

            删除操作：
                1. 需要操作两张表，setmeal和setmeal_dish
                2. setmeal，直接通过id删除
                3. setmeal_dish，根据setmealId删除
        */
        setMealService.deleteWithMealDish(ids);
        return R.success("删除成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getByIdWithDish(@PathVariable Long id) {
        log.info("id = " + id);
        SetmealDto dto = setMealService.getByIdWithDish(id);
        return R.success(dto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("dto = " + setmealDto);
        setMealService.updateWithDish(setmealDto);

        return R.success("修改成功");
    }


    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {

        // 通过分类id，查询套餐和菜品
        // 需要操作setmeal、setmeal_dish

        // 构建条件构造器
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        wrapper.eq(Setmeal::getStatus, 1);
        wrapper.orderByDesc(Setmeal::getUpdateTime);


        List<Setmeal> list = setMealService.list(wrapper);


        if (list != null) {
            return R.success(list);
        }

        return R.error("暂未添加套餐");
    }
}
