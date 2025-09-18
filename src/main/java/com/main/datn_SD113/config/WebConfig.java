package com.main.datn_SD113.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình cho uploads với cache validation
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCachePeriod(300) // Giảm cache xuống 5 phút
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));

        // Cấu hình cho images với cache tối ưu
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(1800); // 30 phút cho static images

        // Client-static với cache ngắn hạn
        registry.addResourceHandler("/client-static/**")
                .addResourceLocations("classpath:/static/client-static/")
                .setCachePeriod(60); // 1 phút thay vì 0

        // Cấu hình cho static resources khác
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // Thêm cấu hình cho static resources tổng quát
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
