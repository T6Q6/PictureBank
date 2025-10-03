package com.sct.picturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
//        允许前端请求携带Cookie 等凭证信息。
                .allowCredentials(true)
//        指定允许哪些域名的前端页面来跨域调用接口。
                .allowedOriginPatterns("*")
//        指定允许的 HTTP 请求方法。
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//        指定允许前端请求中携带的 HTTP 头信息。
                .allowedHeaders("*")
//        指定后端响应中，哪些 HTTP 头可以被前端 JavaScript 代码访问到。
                .exposedHeaders("*");
    }
}
