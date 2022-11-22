package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService service;

    /**
     * 添加分类操作
     *
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        boolean save = service.save(category);
        if (save) {
            return R.success("新增分类成功");
        }
        return R.error("新增分类失败");
    }

    /**
     * 分类分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        // 构建分页构造器
        Page<Category> pageInfo = new Page<Category>(page, pageSize);
        // 构建条件构造器
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        // 设置排序
        wrapper.orderByAsc(Category::getSort);

        service.page(pageInfo);

        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("执行根据ID删除方法");
        // 调用remove方法
        service.remove(ids);
//        boolean res = service.removeById(ids);

        return R.success("删除成功");

    }

    /**
     * 分类修改方法
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        boolean res = service.updateById(category);

        if (res) {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    @GetMapping("/list")
    public R<List<Category>> ListByType(Category category) {
        // 创建条件构造器
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getType() != null ,Category::getType,category.getType());
        // 设置排序方式
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        // 执行查询方法
        List<Category> categories = service.list(wrapper);

        if (categories.size() > 0) {
            return R.success(categories);
        }
        return R.error("查询失败");
    }
}
