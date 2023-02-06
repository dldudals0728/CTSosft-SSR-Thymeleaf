package com.ctsoft.tokenLogin.tokenLoginEx.oauth;

import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {
    private final OauthProperties properties;
    public OauthConfig(OauthProperties properties) {
        this.properties = properties;
    }
}