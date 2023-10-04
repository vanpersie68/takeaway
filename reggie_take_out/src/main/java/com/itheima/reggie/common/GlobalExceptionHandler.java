package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/*全局异常处理: 将异常的原因反馈到前端页面上*/
//可以拦截使用 @RestController 和 @Controller注解 的类，如果抛出异常，就会进行处理
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler
{
    //进行异常处理方法
    //@ExceptionHandler 用来表示这个方法处理 括号中的这种异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> excepitonHandler(SQLIntegrityConstraintViolationException ex)
    {
        //异常信息
        log.error(ex.getMessage());

        //错误的提示信息： Duplicate entry 'zhangsan' for key 'idx_username' (unique字段，重复的用户名)
        if(ex.getMessage().contains("Duplicate entry"))
        {
            //根据空格分割字符串
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    //捕获我们自己定义的异常
    @ExceptionHandler(CustomException.class)
    public R<String> excepitonHandler(CustomException ex)
    {
        //异常信息
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
