package com.ctsoft.tokenLogin.tokenLoginEx.repository;

import com.ctsoft.tokenLogin.tokenLoginEx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(String userId);
}
