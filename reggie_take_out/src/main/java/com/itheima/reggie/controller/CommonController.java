package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

//负责文件上传与下载
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController
{
    @Value("${reggie.path}")
    private String basePath;

    //文件上传:页面发送请求进行图片上传，请求服务端将图片保存到本地服务器
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException
    {
        //file 是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info("file:{}",file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取原始文件名的后缀名 如.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID随机生成文件名，防止因为文件名相同造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists())
        {
            //目录不存在,进行创建
            dir.mkdirs();
        }

        //将临时文件转存到指定的一个位置
        file.transferTo(new File(basePath + fileName));
        return R.success(fileName);
    }

    //文件下载:页面发送请求进行图片下载，将上传的图片进行回显 到浏览器页面
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)
    {
        //输入流，通过输入流读取文件内容
        try
        {
            FileInputStream fileInputStream = new FileInputStream
                    (new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器，在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            byte[] bytes = new byte[1024];
            int length = 0;
            while((length = fileInputStream.read(bytes)) != -1)
            {
                outputStream.write(bytes,0,length);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
