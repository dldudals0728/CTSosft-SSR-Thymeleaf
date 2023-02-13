package com.ctsoft.tokenLogin.tokenLoginEx.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

// 값을 바인딩 할 수 있는 상탱로 만들어준다.
// application.yml 파일의 정보들을 객체로 바인딩한다.
// oauth2 하위에 크게 user 와 provider 가 존재하기 때문에 하위에 존재하는 값들을 static class의 필드로 두어 값을 바인딩 받을 수 있는 상태로 만든다.
// OauthProperties(this)를 이용하여 값을 바인딩 할 수 있는 상태로 만들었으면, 실제로 사용하기 위해 설정 파일(OauthConfig)을 만들어 주고,
// @EnableConfigurationProperties annotation을 붙여 사용해준다.
@Getter
@ConfigurationProperties(prefix = "oauth2")
public class OauthProperties {
    private final Map<String, User> user = new HashMap<>();
    private final Map<String, Provider> provider = new HashMap<>();
    @Getter
    @Setter
    public static class User {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
    @Getter
    @Setter
    public static class Provider {
        private String tokenUri;
        private String userInfoUri;
        private String userNameAttribute;
    }
}
