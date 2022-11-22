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
import java.awt.datatransfer.FlavorEvent;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传和下载
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${regiee.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file 是一个临时文件，若不进行转存，会在请求结束后丢弃
        log.info(file.toString());

        // 获取源文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 为了避免文件名重复导致文件丢失，使用UUID重新生成文件名
        String fileName = UUID.randomUUID().toString() + suffix;

        // 若目录不存在，需要创建目录
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 动态指定转存目录，使用配置文件
            // 将文件转存至指定目录
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }


    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        // 获取文件名，在服务器中找到此文件
        FileInputStream inputStream = null;
        ServletOutputStream outputStream = null;
        try {
            // 创建字符输入流
            inputStream = new FileInputStream(basePath + name);
            // 获取字符输出流
            outputStream = response.getOutputStream();

            // 设置响应图片
            response.setContentType("image/jpeg");

            // 读取数据
            byte[] bytes = new byte[1024];
            int len = 0;

            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
