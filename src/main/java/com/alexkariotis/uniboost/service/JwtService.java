package com.alexkariotis.uniboost.service;

import com.alexkariotis.uniboost.common.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This is a service that validates, creates and extracts info from JWT
 */
@Service
public class JwtService {



    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }


    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Taking the token extract the expiration date from Claims and compare it with current date.
     * @param token Given JWT Token
     * @return true for expired OR false for Valid.
     */
    private boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Firstly, the client needs to send some data to verify himself in jwt token, after that
     * backend sends to client a unique token that has been identified and has limited identified period
     * asks the system to identify himself again.
     * @param claims
     * @param userDetails
     * @return
     */
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
         return Jwts
                 .builder()
                 .claims(claims)
                 .subject(userDetails.getUsername())
                 .issuedAt(new Date(System.currentTimeMillis()))
                 .expiration(new Date(System.currentTimeMillis() + Constants.EXPIRATION_TIME))
                 .signWith(getSignInKey())
                 .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignInKey() {

        byte[] keyBytes = Decoders.BASE64.decode(Constants.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
