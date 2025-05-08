package com.acs.authentication.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.acs.authentication.util.JwtSessionFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<JwtSessionFilter> jwtFilter() {
		FilterRegistrationBean<JwtSessionFilter> registrationBean = new FilterRegistrationBean<>();

		registrationBean.setFilter(new JwtSessionFilter());
		registrationBean.addUrlPatterns("/api/cloudstack/*");
		registrationBean.setOrder(1);

		return registrationBean;
	}
}
