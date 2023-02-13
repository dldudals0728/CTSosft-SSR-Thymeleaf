package com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.OauthRole;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String name;
    private String email;
    private String imageUrl;
    private OauthRole role;
    private String tokenType;
    private String accessToken;
//    private String refreshToken;

    @Builder
    public LoginResponse(Long id, String name, String email, String imageUrl, OauthRole role, String tokenType, String accessToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.role = role;
        this.tokenType = tokenType;
        this.accessToken = accessToken;
//        this.refreshToken = refreshToken;
    }
}
