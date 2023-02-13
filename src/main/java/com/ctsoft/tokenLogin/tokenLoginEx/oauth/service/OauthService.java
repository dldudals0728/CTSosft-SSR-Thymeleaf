package com.ctsoft.tokenLogin.tokenLoginEx.oauth.service;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.OauthAttributes;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.Member;
import com.ctsoft.tokenLogin.tokenLoginEx.jwt.JwtTokenProvider;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.InMemoryProviderRepository;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.OauthProvider;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto.OauthTokenResponse;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto.UserProfile;
import com.ctsoft.tokenLogin.tokenLoginEx.oauth.dto.LoginResponse;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.MemberRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Service
public class OauthService {
    private final InMemoryProviderRepository inMemoryProviderRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public OauthService(InMemoryProviderRepository inMemoryProviderRepository,
                        JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.inMemoryProviderRepository = inMemoryProviderRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public LoginResponse login(String providerName, String code) {
        System.out.println("[OauthService] login F");
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
        System.out.println(provider);
        System.out.println(provider.toString());

        // access token 가져오기
        OauthTokenResponse tokenResponse = this.getToken(code, provider);

        // 유저 정보 가져오기
        UserProfile userProfile = this.getUserProfile(providerName, tokenResponse, provider);

        // 유저 DB에 저장
        Member member = this.saveOrUpdate(userProfile);

        // create JWT
        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(member.getId()));

        return LoginResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .imageUrl(member.getImageUrl())
                .role(member.getRole())
                .tokenType("Bearer")
                .accessToken(accessToken)
                .build();
    }

    private OauthTokenResponse getToken(String code, OauthProvider provider) {
        return WebClient.create()
                .post()
                .uri(provider.getTokenUrl())
                .headers(header -> {
                    header.setBasicAuth(provider.getClientId(), provider.getClientSecret());
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(this.tokenRequest(code, provider))
                .retrieve()
                .bodyToMono(OauthTokenResponse.class)
                .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, OauthProvider provider) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", provider.getRedirectUrl());

        return formData;
    }

    private UserProfile getUserProfile(String providerName, OauthTokenResponse tokenResponse, OauthProvider provider) {
        Map<String, Object> userAttributes = this.getUserAttributes(provider, tokenResponse);
        // 유저 정보(map)를 통해 UserProfile 만들기
        return OauthAttributes.extract(providerName, userAttributes);
    }

    // OAuth 서버에서 유저 정보 map으로 가져오기
    private Map<String, Object> getUserAttributes(OauthProvider provider, OauthTokenResponse tokenResponse) {
        return WebClient.create()
                .get()
                .uri(provider.getUserInfoUrl())
                .headers(header -> header.setBearerAuth(tokenResponse.getAccessToken()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    private Member saveOrUpdate(UserProfile userProfile) {
        Member member = memberRepository.findByOauthId(userProfile.getOauthId())
                .map(entity -> entity.update(
                        userProfile.getEmail(), userProfile.getName(), userProfile.getImageUrl()
                ))
                .orElseGet(userProfile::toMember);

        return member;
    }
}
