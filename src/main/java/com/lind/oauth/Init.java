package com.lind.oauth;

import com.google.common.collect.Sets;
import com.lind.oauth.domain.Authority;
import com.lind.oauth.domain.Role;
import com.lind.oauth.domain.User;
import com.lind.oauth.repository.AuthorityRepository;
import com.lind.oauth.repository.RoleRepository;
import com.lind.oauth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化.
 */
@Component
@AllArgsConstructor
public class Init implements CommandLineRunner {
  private UserService userService;
  private AuthorityRepository authorityRepository;
  private RoleRepository roleRepository;

  @Override
  public void run(String... strings) throws Exception {

    //权限
    Authority authority = new Authority();
    authority.setName("查询");
    authority.setValue("read");
    authorityRepository.save(authority);
    Authority authority2 = new Authority();
    authority2.setName("添加");
    authority2.setValue("write");
    authorityRepository.save(authority2);

    //角色
    Role admin = new Role();
    admin.setName("管理员");
    admin.setValue("ROLE_ADMIN");
    admin.setAuthorities(Sets.newHashSet(authority, authority2));
    roleRepository.save(admin);

    Role role = new Role();
    role.setName("普通用户");
    role.setValue("ROLE_USER");
    role.setAuthorities(Sets.newHashSet(authority));
    roleRepository.save(role);


    //用户
    User fpf = new User();
    fpf.setUsername("admin");
    fpf.setPassword("123");
    fpf.setRoles(Sets.newHashSet(admin));
    userService.createUser(fpf);

    User wl = new User();
    wl.setUsername("zzl");
    wl.setPassword("123");
    wl.setRoles(Sets.newHashSet(role));
    userService.createUser(wl);


  }
}
