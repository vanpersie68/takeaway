package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    //新增菜品 或 套餐分类
    @PostMapping
    public R<String> save(@RequestBody Category category)
    {
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页显示分类
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize)
    {
        //构造分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);

        //构造条件构造器
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();

        //添加排序条件
        queryWrapper.orderByAsc("sort");

        //执行查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    //根据id删除分类
    @DeleteMapping
    public R<String> delete(Long ids)
    {
        log.info("删除分类，id为{}",ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    //根据id修改分类信息
    @PutMapping
    public R<String> update(@RequestBody Category category)
    {
        log.info("修改分类信息，{}",category);
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }

    //根据条件查询分类数据,这个功能主要体现在 菜品管理的添加菜品/套餐管理的添加套餐 页面
    @GetMapping("/list")
    public R<List<Category>> list(Category category)
    {
        //条件构造器
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,"type",category.getType());
        //添加排序条件
        queryWrapper.orderByAsc("sort").orderByAsc("update_time");
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
