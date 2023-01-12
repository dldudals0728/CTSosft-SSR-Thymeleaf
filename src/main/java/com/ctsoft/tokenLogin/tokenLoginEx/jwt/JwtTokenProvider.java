package com.ctsoft.tokenLogin.tokenLoginEx.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static String secret = "leeyoungmin";

    // 5분 단위(for test)
    public static final long JWT_TOKEN_VALIDITY = 1000 * 60 * 15;

    // token 으로 사용자 id 조회
    public String getUsernameFromToken(String token) {
        System.out.println("[JwtTokenProvider] getUsernameFromToken ============");
        return this.getClaimFromToken(token, Claims::getId);
    }

    // token 으로 사용자 속성정보 조회
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        System.out.println("[JwtTokenProvider] getClaimFromToken ============");
        final Claims claims = this.getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 모든 token 에 대한 사용자 속성정보 조회
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    ////////////////////////////////////////////////////////////////////////////////////
    public Claims simpleGetClaimFromToken(String token) {
        System.out.println("[JwtTokenProvider] simpleGetClaimFromToken ============");
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature.");
            return null;
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token.");
            return null;
        } catch (ExpiredJwtException e) {
            System.out.println("Expired JWT token.");
            return null;
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT token.");
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("JWT token compact of handler are invalid.");
            return null;
        }
    }

    public String getTokenInfoFromToken(String token, String prop) {
        return (String) simpleGetClaimFromToken(token).get(prop);
    }
    ////////////////////////////////////////////////////////////////////////////////////

    // token 만료 여부 체크
    private Boolean isTokenExpired(String token) {
        System.out.println("[JwtTokenProvider] isTokenExpired ============");
        final Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // token 만료일자 조회
    public Date getExpirationDateFromToken(String token) {
        System.out.println("[JwtTokenProvider] getExpirationDateFromToken ============");
        return this.getClaimFromToken(token, Claims::getExpiration);
    }

    // id 를 입력받아 accessToken 생성
    public String generateAccessToken(String id) {
        System.out.println("[JwtTokenProvider] generateAccessToken ============");
        return this.generateAccessToken(id, new HashMap<>());
    }

    // id, 속정정보를 이용해 accessToken 생성
    public String generateAccessToken(String id, Map<String, Object> claims) {
        return this.doGenerateAccessToken(id, claims);
    }

    // JWT accessToken 생성
    private String doGenerateAccessToken(String id, Map<String, Object> claims) {
        System.out.println("[JwtTokenProvider] doGenerateAccessToken ============");
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setId(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))   // 5분
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        return accessToken;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            System.out.println("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT token compact of handler are invalid.");
        }

        return false;
    }
}
