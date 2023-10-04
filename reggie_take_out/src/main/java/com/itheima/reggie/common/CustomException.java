package com.itheima.reggie.common;

//自定义的异常
public class CustomException extends RuntimeException
{
    public CustomException(String message)
    {
        super(message);
    }
}
