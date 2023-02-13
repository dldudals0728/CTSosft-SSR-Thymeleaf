package com.ctsoft.tokenLogin.tokenLoginEx.oauth;

import java.util.HashMap;
import java.util.Map;

public class OauthAdapter {
    private OauthAdapter() {}

    // OauthProperties를 OauthProvider로 변환한다.
    public static Map<String, OauthProvider> getOauthProviders(OauthProperties properties) {
        System.out.println("[OauthAdapter] getOauthProviders F");
        System.out.println(properties.getProvider());
        System.out.println(properties.getUser());
        Map<String, OauthProvider> oauthProvider = new HashMap<>();

        properties.getUser().forEach(
                (key, value) -> oauthProvider.put(
                        key,
                        new OauthProvider(value, properties.getProvider().get(key))
                )
        );

        return oauthProvider;
    }
}
