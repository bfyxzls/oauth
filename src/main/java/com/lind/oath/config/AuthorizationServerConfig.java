package com.lind.oath.config;

import com.lind.oath.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
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
  MyUserDetailsService myUserDetailsService;
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private RedisConnectionFactory connectionFactory;

  @Autowired


  @Bean
  public RedisTokenStore tokenStore() {
    return new RedisTokenStore(connectionFactory);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .authenticationManager(authenticationManager)
        .userDetailsService(myUserDetailsService)//若无，refresh_token会有UserDetailsService is required错误
    ;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    security.allowFormAuthenticationForClients()
        .tokenKeyAccess("permitAll()")
        .checkTokenAccess("isAuthenticated()");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
        .withClient("android")
        .scopes("xx")
        .secret("android")
        .authorizedGrantTypes("password", "authorization_code", "refresh_token")
        .and()
        .withClient("webapp")
        .scopes("xx")
        .authorizedGrantTypes("implicit")
        .and()
        .withClient("browser")
        .authorizedGrantTypes("refresh_token", "password")
        .scopes("ui");
  }
}
