package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/*数据传输对象，由于菜品和口味之间是一对多的关系，所以通过 数据传输对象 来实现展示层
* 和服务层之间的数据传输*/
@Data
public class DishDto extends Dish
{
    //菜品对应的口味数据
    private List<DishFlavor> flavors = new ArrayList<>();
    private String categoryName;
    private Integer copies;
}
