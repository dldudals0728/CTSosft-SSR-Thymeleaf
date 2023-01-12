package com.ctsoft.tokenLogin.tokenLoginEx.controller;

import com.ctsoft.tokenLogin.tokenLoginEx.constant.Role;
import com.ctsoft.tokenLogin.tokenLoginEx.dto.LoginDto;
import com.ctsoft.tokenLogin.tokenLoginEx.entity.User;
import com.ctsoft.tokenLogin.tokenLoginEx.jwt.JwtTokenProvider;
import com.ctsoft.tokenLogin.tokenLoginEx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);

        if(userId == null || userId.equals("")) {
            return "index";
        }
        model.addAttribute("userId", userId);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("errorMessage", "아이디 또는 비밀번호 오류입니다.");
        return "login";
    }

    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @GetMapping("/join/error")
    public String joinError(Model model) {
        model.addAttribute("errorMessage", "중복된 아이디입니다!");
        return "join";
    }

    @GetMapping("/user")
    public String user(HttpServletRequest request, Model model) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/user] userId is null!");
            return "redirect:/login";
        } else if (userId.equals("")) {
            System.out.println("[UserController/user] userId is '\"\"'!");
            return "redirect:/expire";
        }

        User user = userService.getUserWithUserId(userId);
        if (user == null) {
            return "userPage";
        }

        model.addAttribute("userId", user.getUserId());
        model.addAttribute("userEmail", user.getEmail());
        return "userPage";
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        String userId = userService.getUserIdFromToken(request, "jdhToken", 7);
        if (userId == null) {
            System.out.println("[UserController/admin] userId is null!");
            return "adminPage";
        } else if (userId.equals("")) {
            System.out.println("[UserController/admin] userId is '\"\"'!");
            return "redirect:/expire";
        }

        User user = userService.getUserWithUserId(userId);
        if (user == null) {
            return "adminPage";
        }

        if (user.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userId", user.getUserId());
        model.addAttribute("userEmail", user.getEmail());

        return "adminPage";
    }

    @GetMapping("/expire")
    public String expire() {
        return "/tokenExpire";
    }

    @PostMapping("/join")
    public String joinUser(User user) {
        System.out.println(user.toString());
        User joinedUser = userService.joinUser(user);
        if (joinedUser == null) {
            return "redirect:/join/error";
        }
        return "redirect:/";
    }

    @PostMapping("/login")
    public String loginUser(LoginDto loginDto, HttpServletResponse response) {
        if (Objects.equals(loginDto.getId(), "") || Objects.equals(loginDto.getPassword(), "")) {
            return "redirect:/login/error";
        }
        User user = userService.loginUser(loginDto);
        if (user == null) {
            return "redirect:/login/error";
        }
        System.out.println("login control");
        System.out.println(user.getUserId());
        Map<String, Object> myClaims = new HashMap<>();
        myClaims.put("role", "ADMIN");
        String accessToken = URLEncoder.encode(jwtTokenProvider.generateAccessToken(user.getUserId(), myClaims), StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("jdhToken", "Bearer_" + accessToken);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        response.addCookie(cookie);

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 내가 생각하는 로그아웃 3가지 방법
        // 1. Cookie value 제거 -> token 값은 한번 생성되면 제어가 불가능. expire 시킬 수 없기 때문에 cookie value를 제거한다.
        //  - 문제점: cookie value를 삭제하고 다시 로그인하면 똑같은 token 이 만들어지는거 아닌가 ???
        // 2. token 강제 만료 또는 삭제 -> 가능한 지 모르겠다.
        Cookie cookie = new Cookie("jdhToken", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
