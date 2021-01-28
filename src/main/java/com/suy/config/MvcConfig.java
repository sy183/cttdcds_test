package com.suy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan("com.suy.controller")
public class MvcConfig implements WebMvcConfigurer {

    @Value("/WEB-INF/jsp/")
    private String prefix;

    @Value(".jsp")
    private String suffix;

    private final Charset defaultCharset = StandardCharsets.UTF_8;

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix(prefix);
        viewResolver.setSuffix(suffix);
        return viewResolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(messageConverter());
    }

    @Bean
    public StringHttpMessageConverter messageConverter() {
        return new StringHttpMessageConverter(defaultCharset);
    }

}
