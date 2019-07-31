package com.lind.oauth.service;

import com.lind.oauth.domain.User;
import com.lind.oauth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Created by SuperS on 2017/9/25.
 *
 * @author SuperS
 */
@Service
public class UserServiceImpl implements UserService {
  private final Logger log = LoggerFactory.getLogger(getClass());
  @Autowired
  PasswordEncoder bCryptPasswordEncoder;
  @Autowired
  private UserRepository userRepository;

  @Override
  public void createUser(User user) {
    User existing = userRepository.findByUsername(user.getUsername());
    Assert.isNull(existing, "user already exists: " + user.getUsername());

    String hash = bCryptPasswordEncoder.encode(user.getPassword());
    user.setPassword(hash);
    userRepository.save(user);
    log.info("new user has been created: {}", user.getUsername());
  }
}
