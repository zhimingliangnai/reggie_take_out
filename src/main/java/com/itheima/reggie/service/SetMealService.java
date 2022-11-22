package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.pojo.Setmeal;
import org.springframework.transaction.annotation.Transactional;


public interface SetMealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存套餐和菜品关联信息
     * @param setmealDto
     */
    @Transactional
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐和菜品关联信息
     * @param ids
     */
    @Transactional
    void deleteWithMealDish(Long[] ids);

    /**
     * 根据id查询套餐和菜品关联信息
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    /**
     * 修改套餐，同时修改套餐和菜品关联信息
     * @param dto
     */
    @Transactional
    void updateWithDish(SetmealDto dto);
}
