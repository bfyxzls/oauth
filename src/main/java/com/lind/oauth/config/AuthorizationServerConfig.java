package com.lind.oauth.config;

import com.lind.oauth.service.security.JPAUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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
  @Value("${user.oauth.clientId:android1}")
  private String ClientID;
  @Value("${user.oauth.clientSecret:android1}")
  private String ClientSecret;
  @Value("${user.oauth.redirectUris:http://localhost:8081/callback}")
  private String RedirectURLs;

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
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    // 配置token获取和验证时的策略 (Spring Security安全表达式),可以表单提交
    oauthServer.tokenKeyAccess( "permitAll()")
        .checkTokenAccess("isAuthenticated()")
        .allowFormAuthenticationForClients();
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
        .withClient(ClientID)
        .secret(passwordEncoder.encode(ClientSecret))
        .authorizedGrantTypes("authorization_code", "refresh_token", "password", "implicit")
        .scopes("all","read","write","del")
        .redirectUris(RedirectURLs)
        .accessTokenValiditySeconds(1200)
        .refreshTokenValiditySeconds(50000);
  }
}
