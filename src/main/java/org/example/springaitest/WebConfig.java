package org.example.springaitest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**");
            }

            @Override
            public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry){
                registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/statics/")
                        .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
            }
        };
    }
}