package com.lind.oauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 资源授权.
 */
@Configuration
@EnableResourceServer
@Order(6)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()//禁用了 csrf 功能
        .authorizeRequests()//限定签名成功的请求
        .antMatchers("/index").access("#oauth2.hasScope('del')") //授权码scopes里需要选中del才可以访问
        .antMatchers("/user").authenticated()//签名成功后可访问，不受role限制
        .anyRequest().permitAll()//其他没有限定的请求，允许访问
        .and().anonymous()//对于没有配置权限的其他请求允许匿名访问
        .and().formLogin()//使用 spring security 默认登录页面
        .and().httpBasic();//启用http 基础验证

  }
}
