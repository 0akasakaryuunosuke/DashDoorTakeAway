package com.example.common.config;


   import com.alibaba.dashscope.app.Application;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.client.RestTemplate;

   @Configuration
   public class ApplicationConfig {

       @Bean
       public RestTemplate restTemplate() {
           return new RestTemplate();
       }

       // 新增DashScope Application配置
       @Bean
       public Application dashScopeApplication() {
           return new Application();
       }

   }
