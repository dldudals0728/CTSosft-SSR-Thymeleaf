package com.ctsoft.tokenLogin.tokenLoginEx.service;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.Role;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.LoginDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.User;
import com.ctsoft.tokenLogin.tokenLoginEx.jwt.JwtTokenProvider;
import com.ctsoft.tokenLogin.tokenLoginEx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public User joinUser(User user) {
        if (isJoinedUser(user.getUserId())) {
            return null;
        }
        return userRepository.save(user);
    }

    public boolean isJoinedUser(String userId) {
        User user = userRepository.findByUserId(userId);
        return user != null;
    }

    public User loginUser(LoginDto loginDto) {
        User user = userRepository.findByUserId(loginDto.getId());
        if (user == null) {
            return null;
        }
        if (!Objects.equals(user.getPassword(), loginDto.getPassword())) {
            return null;
        }
        return user;
    }

    public String getUserIdFromToken(HttpServletRequest request, String cookieName, int cookiePrefixLength) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.out.println("[UserService]cookies is null !!");
            return null;
        }

        System.out.println("cookies length : " + cookies.length);

        for (Cookie cookie:cookies) {
            System.out.println("Cookie name : " + cookie.getName() + " / " + "Cookie value : " + cookie.getValue());
            if (Objects.equals(cookie.getName(), cookieName)) {
                System.out.println("it has token named \"" + cookieName + "\"");
                String token = cookie.getValue().substring(cookiePrefixLength);
                if (!jwtTokenProvider.validateToken(token)) {
                    return "";
                }
                String userId = jwtTokenProvider.getUsernameFromToken(token);
                System.out.println("userId : " + userId);
                System.out.println("userRole : " + jwtTokenProvider.getTokenInfoFromToken(token, "role"));
                System.out.println(jwtTokenProvider.simpleGetClaimFromToken(token));
                return userId;
            }
        }

        return null;
    }

    public User getUserWithUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public String getUserRoleFromToken(HttpServletRequest request, String cookieName, int cookiePrefixLength) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie:cookies) {
            if (Objects.equals(cookie.getName(), cookieName)) {
                String token = cookie.getValue().substring(cookiePrefixLength);
                if (!jwtTokenProvider.validateToken(token)) {
                    return "";
                }
                String userRole = jwtTokenProvider.getTokenInfoFromToken(token, "role");
                System.out.println("userRole : " + userRole);
                System.out.println(jwtTokenProvider.simpleGetClaimFromToken(token));
                return userRole;
            }
        }

        return null;
    }
}
