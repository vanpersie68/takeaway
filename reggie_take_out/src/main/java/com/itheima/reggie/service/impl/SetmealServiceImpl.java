package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService
{
    @Autowired
    private SetmealDishService setmealDishService;

    //新增套餐，同时要保持与菜品的关联关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto)
    {
        //保存套餐基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        //获取添加的套餐中菜品的信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //给添加的菜品信息 添加套餐的id
        for(SetmealDish setmealDish : setmealDishes)
        {
            setmealDish.setSetmealId(setmealDto.getId());
        }

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    //删除套餐，同时删除套餐和菜品的关联数据
    @Override
    public void removeWithDish(List<Long> ids)
    {

        for(Long id : ids)
        {
            Setmeal setmeal = this.getById(id);
            //查询套餐的状态，确定是否可以删除。
            if(setmeal.getStatus() == 0)
            {
                //如果可以删除，先删除套餐表中的数据 setmeal
                this.removeById(id);
                //删除关系表中的数据----setmeal_dish
                QueryWrapper<SetmealDish> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("setmeal_id",id);
                setmealDishService.remove(queryWrapper);
            }
            else
            {
                //如果不能删除，直接抛出一个业务异常
                throw new CustomException("套餐正在售卖中，不能删除");
            }
        }
    }

    @Override
    public SetmealDto getByIdWithDish(Long id)
    {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询套餐的菜品信息
        QueryWrapper<SetmealDish> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("setmeal_id",setmeal.getId());

        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    //修改套餐,同时更改套餐对应的菜品
    @Override
    public void updateWithDish(SetmealDto setmealDto)
    {
        //更新setmeal表基本信息
        this.updateById(setmealDto);

        //更新setmeal_dish表信息delete操作 (清除当前套餐对应菜品数据)
        QueryWrapper<SetmealDish> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("setmeal_id",setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //更新setmeal_dish表信息insert操作 (添加当前套餐对应菜品数据)
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishes)
        {
            setmealDish.setSetmealId(setmealDto.getId());
        }

        setmealDishService.saveBatch(setmealDishes);
    }
}
