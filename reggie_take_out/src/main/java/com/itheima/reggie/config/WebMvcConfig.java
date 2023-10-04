package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

// 这个类在项目启动时就会调用
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport
{
    /**
     * 设置静态资源映射: 能够让tomcat服务器访问resources下面的backend 和 front文件中的html文件，否则会出现404报错
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /*扩展mvc框架的消息转换器，用处是为了进行Java对象到json数据的转换*/
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        log.info("扩展我们自己的消息转换器...");
        //创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java转换为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }
}
