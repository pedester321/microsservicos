package com.microsservicos.productapi.modules.jwt.service;


import com.microsservicos.productapi.config.exception.AuthenticationException;
import com.microsservicos.productapi.config.exception.ValidationException;
import com.microsservicos.productapi.modules.jwt.dto.JwtResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.lang.Strings;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class JwtService {

    private static final String EMPTY_SPACE = " ";
    private static final Integer TOKEN_INDEX = 1;

    @Value("${app-config.secrets.api-secret}")
    private String apiSecret;

    public void validateAuthorization(String token) {
        var accessToken = extractToken(token);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8))) // Substitui setSigningKey
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            // Validação de usuário
            var user = JwtResponse.getUser(claims);

            if (isEmpty(user) || isEmpty(user.getId())) {
                throw new ArithmeticException("User is not valid.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationException("Error trying to process the Access Token.");
        }
    }

    private String extractToken(String token) {
        if (isEmpty(token)) {
            throw new AuthenticationException("The access token was not informed.");
        }
        if (token.contains(EMPTY_SPACE)) {
            return token.split(EMPTY_SPACE)[TOKEN_INDEX];
        }
        return token;
    }
}
