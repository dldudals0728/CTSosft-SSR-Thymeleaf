package com.ctsoft.tokenLogin.tokenLoginEx.oauth;

import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Configuration
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {
    private final OauthProperties properties;
    public OauthConfig(OauthProperties properties) {
        this.properties = properties;
    }

    // OauthConfig.java 추가 코드
    @Bean
    public InMemoryProviderRepository inMemoryProviderRepository() {
        Map<String, OauthProvider> providers = OauthAdapter.getOauthProviders(this.properties);
        return new InMemoryProviderRepository(providers);
    }
}