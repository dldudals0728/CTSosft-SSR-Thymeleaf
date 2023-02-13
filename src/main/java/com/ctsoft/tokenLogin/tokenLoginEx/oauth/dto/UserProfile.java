package com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.OauthRole;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfile {
    private final String oauthId;
    private final String email;
    private final String name;
    private final String imageUrl;

    @Builder
    public UserProfile(String oauthId, String email, String name, String imageUrl) {
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Member toMember() {
        return Member.builder()
                .oauthId(this.oauthId)
                .email(this.email)
                .name(this.name)
                .imageUrl(this.imageUrl)
                .role(OauthRole.GUEST)
                .build();
    }
}
