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


# oauth2使用过程
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