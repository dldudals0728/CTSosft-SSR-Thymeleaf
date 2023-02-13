package com.ctsoft.tokenLogin.tokenLoginEx.repository;

import com.ctsoft.tokenLogin.tokenLoginEx.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthId(String oauthId);
}
