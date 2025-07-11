package com.keakimleang.bulkpayment.utils;

import com.keakimleang.bulkpayment.config.props.TokenProperties;
import com.keakimleang.bulkpayment.securities.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenUtil {
    @Value("${token.issuer}")
    private String issuer;
    @Value("${token.secret}")
    private String secret;

    private final TokenProperties prop;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(CustomUserDetails ud) {
        return generateToken(ud, prop.getAccessTokenExpiresHours());
    }

    public String generateRefreshToken(CustomUserDetails ud) {
        return generateToken(ud, prop.getRefreshTokenExpiresHours());
    }

    public String generateToken(CustomUserDetails ud, Long expiration) {
        String username = ud.getUsername();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expiration * 3600000);
        SecretKey key = getSecretKey();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect");
        } catch (ExpiredJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT token compact of handler are invalid.");
        }
    }

    public boolean isTokenNotExpired(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Instant getExpirationDateFromToken(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return claims.getExpiration().toInstant();
        } catch (Exception e) {
            return null;
        }
    }
}
