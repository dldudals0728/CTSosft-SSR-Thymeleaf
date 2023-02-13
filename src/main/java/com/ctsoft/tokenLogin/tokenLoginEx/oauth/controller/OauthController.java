package com.ctsoft.tokenLogin.tokenLoginEx.oauth.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto.LoginResponse;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.service.OauthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class OauthController {

    private final OauthService oauthService;

    public OauthController(OauthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/login/oauth2/code/{provider}")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@PathVariable String provider, @RequestParam String code) {
        System.out.println("[OauthController] login oauth2 login redirect url");
        System.out.println("provider: " + provider);
        System.out.println("code: " + code);
        LoginResponse loginResponse = oauthService.login(provider, code);
        return ResponseEntity.ok().body(loginResponse);
    }

    @GetMapping("/oauth/login")
    public String oauthLoginPage() {
        return "oauth/oauth-index";
    }
}
