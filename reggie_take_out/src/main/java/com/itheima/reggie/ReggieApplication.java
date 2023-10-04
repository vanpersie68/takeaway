package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@Slf4j //可以直接使用log,用来添加日志内容（由lombok提供的）
@SpringBootApplication
@ServletComponentScan //扫描Servlet用到的注解：如过滤器; 否则无法生效
public class ReggieApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功");
    }
}
