package com.ctsoft.tokenLogin.tokenLoginEx.constant;

public enum OauthRole {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER");
    private final String key;

    OauthRole(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
