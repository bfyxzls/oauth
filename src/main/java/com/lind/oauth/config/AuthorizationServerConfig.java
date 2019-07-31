package com.lind.oauth.config;

import com.lind.oauth.service.security.JPAUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private RedisConnectionFactory connectionFactory;
  @Autowired
  private JPAUserDetailsService jpaUserDetailsService;

  @Bean
  public RedisTokenStore tokenStore() {
    return new RedisTokenStore(connectionFactory);
  }

  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .authenticationManager(authenticationManager)
        .userDetailsService(jpaUserDetailsService)//若无，refresh_token会有UserDetailsService is required错误
        .tokenStore(tokenStore());
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    security.allowFormAuthenticationForClients()
        .tokenKeyAccess("permitAll()")
        .checkTokenAccess("isAuthenticated()");


  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    //授权客户端这块，可以存储到数据库里，每个客户都有自己的clientId,secret,scopes等.
    clients.inMemory()
        .withClient("android1")
        .secret(passwordEncoder.encode("android1"))//springboot2.1以后用这种方式
        .scopes("*")
        .authorizedGrantTypes("password", "authorization_code", "refresh_token");
  }
}
