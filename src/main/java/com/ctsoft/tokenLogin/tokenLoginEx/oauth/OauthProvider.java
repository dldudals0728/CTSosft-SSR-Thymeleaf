package com.ctsoft.tokenLogin.tokenLoginEx.oauth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

// spring security의 ClientRegistration 역할
@Getter
@ToString
public class OauthProvider {
    private final String clientId;
    private final String clientSecret;
    private final String redirectUrl;
    private final String tokenUrl;
    private final String userInfoUrl;

    public OauthProvider(OauthProperties.User user, OauthProperties.Provider provider) {
        this(user.getClientId(), user.getClientSecret(), user.getRedirectUri(), provider.getTokenUri(), provider.getUserInfoUri());
        System.out.println("[OauthProvider] OauthProvider F");
        System.out.println(user);
        System.out.println(provider);
    }

    @Builder
    public OauthProvider(String clientId, String clientSecret, String redirectUrl, String tokenUrl, String userInfoUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.tokenUrl = tokenUrl;
        this.userInfoUrl = userInfoUrl;
    }
}
