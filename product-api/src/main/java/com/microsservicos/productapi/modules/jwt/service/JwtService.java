package com.microsservicos.productapi.modules.jwt.service;


import com.microsservicos.productapi.config.exception.AuthenticationException;
import com.microsservicos.productapi.modules.jwt.dto.JwtResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class JwtService {

    private static final String BEARER = "Bearer ";
    private static final Integer TOKEN_INDEX = 1;

    @Value("${app-config.secrets.api-secret}")
    private String apiSecret;

    public void validateAuthorization(String token) {
        var accessToken = extractToken(token);

        SignatureAlgorithm sa = SignatureAlgorithm.HS256;
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), sa.getJcaName());
        //Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8))
        try {
            var claims = Jwts.parser()
                    .setSigningKey(secretKeySpec)
                    .build().parseSignedClaims(accessToken)
                    .getBody();
            var user = JwtResponse.getUser(claims);
            if (isEmpty(user) || isEmpty(user.getId())) {
                throw new AuthenticationException("User is not valid.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //log.error(accessToken);
            throw new AuthenticationException("Error trying to process the Access Token.");
        }
    }

    private String extractToken(String token) {
        if (isEmpty(token)) {
            throw new AuthenticationException("The access token was not informed.");
        }
        if (token.startsWith(BEARER)) {
            token = token.substring(7).trim();
        }
        return token;
    }
}
