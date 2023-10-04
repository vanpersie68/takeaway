package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController
{
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /*新增菜品*/
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto)
    {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    //菜品信息的分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name)
    {
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //构造条件构造器
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();

        //添加查询条件 (如果name不为空，才会添加这个条件)
        queryWrapper.like(!StringUtils.isEmpty(name),"name",name);

        //添加排序条件
        queryWrapper.orderByDesc("update_time");

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        /*由于网页中对于菜品的类型显示的是categoryName，而dish中我们存储的是categoryId
        导致网页无法输出菜品的类型； 所以我们通过在DishDto中增加categoryName的字段。
        我们首先将pageInfo进行对象拷贝，拷贝到dishDtoPage中，再给dishDtoPage中的
         categoryName赋值，最后将dishDtoPage返回给前端页面*/
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //将pageInfo 除了records的属性都拷贝给dishDtoPage

        //获取pageInfo 中的 records
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = new LinkedList<>();

        for (Dish record : records)
        {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record,dishDto);
            Long categoryId = record.getCategoryId(); //分类id
            Category category = categoryService.getById(categoryId); //根据分类id获取到这个分类
            if(category != null)
            {
                String categoryName = category.getName(); //获取该分类的名称
                dishDto.setCategoryName(categoryName); //将分类的名称赋值给 dishDto 中的categoryName属性
            }
            list.add(dishDto);
        }

        //给dishDtoPage 中的 records赋值
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    //删除 和 批量删除 菜品 (因为是多个id，所以使用容器)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids)
    {
        log.info("ids:{}",ids);
        dishService.removeWithFlavor(ids);
        return R.success("删除成功");
    }

    //停售菜品
    @PostMapping("/status/{status}")
    public R<String> sale(@RequestParam List<Long> ids,@PathVariable int status)
    {
        log.info("ids:{}",ids);
        for(Long id : ids)
        {
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("菜品状态修改成功");
    }

    //-----------修改菜品的模块------------------
    //根据id来查询菜品信息和口味信息(在修改菜品信息的时候实现菜品信息回显的步骤)
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id)
    {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    //修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto)
    {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    //----------------------套餐管理页面与菜品数据关联的代码-------------------
    //根据条件 查询对应菜品数据 (在套餐管理-添加套餐-添加菜品中应用)
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish)
    {
        //构造查询条件
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        //添加条件，查询状态为1的（起售状态）
        queryWrapper.eq("status",1);
        queryWrapper.eq(dish.getCategoryId() != null,
                "category_id",dish.getCategoryId());
        queryWrapper.orderByAsc("sort").orderByDesc("update_time");

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = new LinkedList<>();

        for (Dish record : list)
        {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(record,dishDto);
            Long categoryId = record.getCategoryId(); //分类id
            Category category = categoryService.getById(categoryId); //根据分类id获取到这个分类
            if(category != null)
            {
                String categoryName = category.getName(); //获取该分类的名称
                dishDto.setCategoryName(categoryName); //将分类的名称赋值给 dishDto 中的categoryName属性
            }

            //-----根据菜品id查询当前菜品对应的口味有哪些(这部分是后续的升级，为了让其在移动端显示)------
            //当前菜品的id
            Long dishId = record.getId();
            QueryWrapper<DishFlavor> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("dish_id",dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavorList);
            dishDtoList.add(dishDto);
        }

        return R.success(dishDtoList);
    }
}
