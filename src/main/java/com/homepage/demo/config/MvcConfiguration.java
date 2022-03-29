package com.homepage.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer{

	@Autowired
	DefaultInteceptor defaultInterceptor;
	
	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/templates/", "classpath:/static/");
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(defaultInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/css/**")
				.excludePathPatterns("/fonts/**")
				.excludePathPatterns("/images/**")
				.excludePathPatterns("/images_new/**")
				.excludePathPatterns("/javascripts/**")
				.excludePathPatterns("/lib/**")
				.excludePathPatterns("/STATIC/**")
				.excludePathPatterns("/templates/**");
	}
}
