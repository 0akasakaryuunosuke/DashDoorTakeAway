package com.example;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.example.mapper")

public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);


    /*    Application app = new Application();
        ApplicationParam param = ApplicationParam.builder()
                .apiKey("sk-d80397db309349e6becd02b5975cd649")
                .appId("a69376d7952b42359a589754e2bca7d3")
                .prompt("你好")
                .build();

        try {
            app.streamCall(param).blockingForEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace(); // 查看原始错误堆栈
        }*/
    }



}
