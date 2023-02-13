package com.ctsoft.tokenLogin.tokenLoginEx.entity;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.OauthRole;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table
@Getter
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String oauthId;
    private String name;
    private String email;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private OauthRole role;

    protected Member() {}

    @Builder
    public Member(String oauthId, String name, String email, String imageUrl, OauthRole role) {
        this(null, oauthId, name, email, imageUrl, role);
    }

    public Member(Long id, String oauthId, String name, String email, String imageUrl, OauthRole role) {
        this.id = id;
        this.oauthId = oauthId;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    public Member update(String name, String email, String imageUrl) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
