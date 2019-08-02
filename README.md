# springboot里使用oauth2
> springboot-2.0.6.RELEASE+springcloud-Finchley.SR2在使用oauth2时，
会出现加密和redis token的问题，而在升级到2.1.4.RELEASE+Greenwich.SR1之后，
问题得到了解决，这应该是spring的一个bug，在升级后他们自己解决了这个bug.

# 依赖的类库
```
implementation 'org.springframework.cloud:spring-cloud-starter-oauth2'
implementation 'org.springframework.cloud:spring-cloud-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```
> 使用redis进行token的存储，用户授权相关表使用mysql存在


# oauth2-账号密码登陆
1. 登陆，获取access
```
POST /oauth/token?grant_type=password&amp; password=123&amp; username=zzl HTTP/1.1
Host: localhost:8004
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: Basic YW5kcm9pZDE6YW5kcm9pZDE=
User-Agent: PostmanRuntime/7.15.2
Accept: */*
Cache-Control: no-cache
Postman-Token: 96298691-8522-44fc-ae86-7862139bb266,879253b3-51a7-4f70-b00c-3227b2cbe2db
Host: localhost:8004
Cookie: JSESSIONID=2A98AF82DBAAD670C562F1CCE45D2827
Accept-Encoding: gzip, deflate
Content-Length: 
Connection: keep-alive
cache-control: no-cache
```
响应
```
{
    "access_token": "c96acdff-3ef6-41c3-b42c-5824f31b4c22",
    "token_type": "bearer",
    "refresh_token": "0404af8f-b756-4e9e-9625-6830105ee0a8",
    "expires_in": 42720,
    "scope": "*"
}
```
2. 使用access_token去获取真正的资源
控制器
```$xslt
  @GetMapping("/user")
  @PreAuthorize("hasRole('admin')")
  public Principal user(Principal user) {
    return user;
  }

  @GetMapping("/index")
  @PreAuthorize("hasAuthority('write')")
  public String index() {
    return "index";
  }
```
调用
```
GET /index HTTP/1.1
Host: localhost:8004
Content-Type: application/json
Authorization: bearer c96acdff-3ef6-41c3-b42c-5824f31b4c22
User-Agent: PostmanRuntime/7.15.2
Accept: */*
Cache-Control: no-cache
Postman-Token: ba14084e-40ed-4f1e-a19e-d48252e905a9,c6211c40-f44d-476a-920c-e8fd76f9d52b
Host: localhost:8004
Cookie: JSESSIONID=2A98AF82DBAAD670C562F1CCE45D2827
Accept-Encoding: gzip, deflate
Connection: keep-alive
cache-control: no-cache
```
响应
```$xslt
httpstatus:200
```
使用没有登陆（没有授权），则响应
```$xslt
httpstatus:401
```
而当这个用户已经登陆（已经授权），但没有对应的权限访问（鉴权后，它的权限不足）,则响应
```$xslt
httpstatus:403
```

# oauth2-授权码登陆
> stringboot从1.5.x升级到2.x之后，spring-security有了很大的调整，也就是说，直接从1.x升级到2.x你的项目是跑不起来的，
需要我们做一些调整，主要调整的就是拦截器的执行顺序，即设置它的Order注解。
1. 授权服务器的AuthorizationServerConfig配置
```$xslt
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
    oauthServer.tokenKeyAccess("permitAll()")
        .checkTokenAccess("isAuthenticated()");

  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
        .withClient(ClientID)
        .secret(passwordEncoder.encode(ClientSecret))
        .authorizedGrantTypes("authorization_code", "refresh_token",
            "password", "implicit")
        .scopes("all")
        .redirectUris(RedirectURLs);
  }
}
```
2. 授权服务器的SecurityConfig配置
> 这里有个坑，需要手动设置过滤器执行顺序为`2`
```$xslt
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
        .antMatchers("/index").hasAnyRole("USER", "ADMIN")
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

```
3. 授权服务器的ResourceServerConfig配置
> 它的执行顺序为6，即它在SecurityConfig之后才去执行
```$xslt
@Configuration
@EnableResourceServer
@Order(6)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()//禁用了 csrf 功能
        .authorizeRequests()//限定签名成功的请求
        .antMatchers("/index").hasAnyRole("USER", "ADMIN")
        .antMatchers("/users").authenticated()//签名成功后可访问，不受role限制
        .anyRequest().permitAll()//其他没有限定的请求，允许访问
        .and().anonymous()//对于没有配置权限的其他请求允许匿名访问
        .and().formLogin()//使用 spring security 默认登录页面
        .and().httpBasic();//启用http 基础验证

  }
}
```
4. 下面说一下实例运行的过程
* 浏览器执行：`http://localhost:8081/oauth/authorize?response_type=code&redirect_uri=http://localhost:8081/callback&client_id=android1&scop=all`，当没有登陆时会提示你去默认的login进行登陆
* 当跳转到security默认的login页面之后，你可以输入自己的账号密码（资源服务器的账号密码，如QQ，微信等）
* 点登陆按钮后，即再次进入oauth/authorize页面，让你对资源进行授权，就是你在`AuthorizationServerConfig`里配置的，本例是`all`
* 提交表单，会进行授权，然后授权服务器通过你的`redirect_uri`对你进行重定向，这时会带着code，这个code只能使用一次，注意你的回径路径应该是permitAll的
* 然后你拿着code去换access_token，注意这是post请求，client_id和client_secret如果在url上发送，如果在AuthorizationServerConfig.configure方法里添加allowFormAuthenticationForClients内容
* 拿着access_token就可以访问资源了
