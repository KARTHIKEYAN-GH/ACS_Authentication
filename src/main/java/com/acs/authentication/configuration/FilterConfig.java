package com.acs.authentication.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.acs.authentication.util.JwtSessionFilter;

@Configuration
public class FilterConfig {

    private final JwtSessionFilter jwtSessionFilter;

    public FilterConfig(JwtSessionFilter jwtSessionFilter) {
        this.jwtSessionFilter = jwtSessionFilter;
    }

    @Bean
    public FilterRegistrationBean<JwtSessionFilter> jwtFilter() {
        FilterRegistrationBean<JwtSessionFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(jwtSessionFilter);  // use injected bean, NOT new instance
        registrationBean.addUrlPatterns("/api/cloudstack/*");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }
}

