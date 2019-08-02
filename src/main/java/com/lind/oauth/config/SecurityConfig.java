package com.lind.oauth.config;

import com.lind.oauth.service.security.JPAUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
@Order(2)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  @Autowired
  private JPAUserDetailsService jpaUserDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/resources/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()//禁用了 csrf 功能
        .authorizeRequests()//限定签名成功的请求
        .antMatchers("/index").hasAnyRole("ROLE_USER", "ROLE_ADMIN")
        .antMatchers("/user").authenticated()//签名成功后可访问，不受role限制
        .anyRequest().permitAll()//其他没有限定的请求，允许访问
        .and().anonymous()//对于没有配置权限的其他请求允许匿名访问
        .and().formLogin()//使用 spring security 默认登录页面
        .and().httpBasic();//启用http 基础验证

  }

  /**
   * 密码加密的实现.
   *
   * @param auth .
   * @throws Exception .
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(jpaUserDetailsService).passwordEncoder(passwordEncoder());
  }

//  @Override
//  public void configure(AuthenticationManagerBuilder authenticationMgr) throws Exception {
// 这是一个测试的账号，不需要数据库支持
//    authenticationMgr.inMemoryAuthentication().withUser("admin").password("admin")
//        .authorities("ROLE_ADMIN");
//  }

  /**
   * 不定义没有password grant_type
   *
   * @return .
   * @throws Exception .
   */
  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
