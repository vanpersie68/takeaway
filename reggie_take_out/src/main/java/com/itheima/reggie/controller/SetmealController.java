package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController
{
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto)
    {
        log.info("套餐信息:{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    //分页查询套餐信息
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //构造条件构造器
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();

        //添加查询条件 (如果name不为空，才会添加这个条件)
        queryWrapper.like(!StringUtils.isEmpty(name),"name",name);

        //添加排序条件
        queryWrapper.orderByDesc("update_time");

        //执行分页查询
        setmealService.page(pageInfo,queryWrapper);

         /*由于网页中对于套餐的类型显示的是categoryName，而setmeal中我们存储的是categoryId
        导致网页无法输出套餐的类型； 所以我们通过在SetmealDto中增加categoryName的字段。
        我们首先将pageInfo进行对象拷贝，拷贝到setmealDtoPage中，再给setmealDtoPage中的
         categoryName赋值，最后将setmealDtoPage返回给前端页面*/
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        //将pageInfo 除了records的属性都拷贝给setmealDtoPage

        //获取pageInfo 中的 records
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = new LinkedList<>();

        for(Setmeal record : records)
        {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record,setmealDto);
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null)
            {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            list.add(setmealDto);
        }

        //给setmealDtoPage 中的 records赋值
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    //删除 和 批量删除 套餐 (因为是多个id，所以使用容器)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids)
    {
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    //停售套餐
    @PostMapping("/status/{status}")
    public R<String> sale(@RequestParam List<Long> ids,@PathVariable int status)
    {
        log.info("ids:{}",ids);
        for(Long id : ids)
        {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("套餐状态修改成功");
    }

    //-----------修改套餐的模块------------------
    //根据id来查询套餐信息和菜品信息(在修改套餐信息的时候实现套餐信息回显的步骤)
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id)
    {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    //修改套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto)
    {
        setmealService.updateWithDish(setmealDto);
        return R.success("修改菜品成功");
    }

    //----------------移动端涉及到的套餐信息模块的代码(套餐不许需要展示其对应的菜品信息)------------------
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal)
    {
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,
                "category_id",setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,
                "status",setmeal.getStatus());
        queryWrapper.orderByDesc("update_time");

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
