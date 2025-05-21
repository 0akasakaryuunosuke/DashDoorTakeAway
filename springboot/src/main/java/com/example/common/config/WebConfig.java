package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements  WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    // 加自定义拦截器JwtInterceptor，设置拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/login")
                  .excludePathPatterns("/wx-login")
                .excludePathPatterns("/register")
                 .excludePathPatterns("/upload")
                .excludePathPatterns("/files/**")
                .excludePathPatterns(
                // 精确匹配SSE接口
                "/ai/assistant-stream",
                // 放行所有错误处理路径
                "/error",
                "/error/**"
                // 其他需要放行的路径
        );
    }
}
