package com.lind.oauth.repository;

import com.lind.oauth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by SuperS on 2017/9/25.
 *
 * @author SuperS
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByUsername(String username);
}
