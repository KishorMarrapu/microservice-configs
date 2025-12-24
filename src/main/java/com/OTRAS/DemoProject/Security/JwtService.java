//package com.OTRAS.DemoProject.Security;
// 
//import java.security.Key;
//
//import java.util.Date;
//
//import java.util.Map;
//
//import java.util.function.Function;
// 
//import org.springframework.security.core.userdetails.UserDetails;
//
//import org.springframework.stereotype.Service;
// 
//import io.jsonwebtoken.Claims;
//
//import io.jsonwebtoken.Jwts;
//
//import io.jsonwebtoken.SignatureAlgorithm;
//
//import io.jsonwebtoken.security.Keys;
// 
//@Service
//
//public class JwtService {
// 
//	private static final String SECRET_KEY = "kE2b9zP7hQxYv5fR1tUo8mC6sN4aJ3wL0gVdB2kF9xR1tP5eQ7yT8uZ9cH3nM6rA";
// 
//	 
//
//    private Key getSigningKey() {
//
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//
//        final String username = extractUsername(token);
//
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//
//    }
// 
// 
//    public String generateToken(String username, Map<String, Object> extraClaims) {
//
//        return Jwts.builder()
//
//                .setClaims(extraClaims)
//
//                .setSubject(username)
//
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day
//
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//
//                .compact();
//
//    }
// 
//    public String extractUsername(String token) {
//
//        return extractClaim(token, Claims::getSubject);
//
//    }
// 
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//
//        final Claims claims = extractAllClaims(token);
//
//        return claimsResolver.apply(claims);
//
//    }
// 
//    private Claims extractAllClaims(String token) {
//
//        return Jwts.parserBuilder()
//
//                .setSigningKey(getSigningKey())
//
//                .build()
//
//                .parseClaimsJws(token)
//
//                .getBody();
//
//    }
// 
//    public boolean isTokenValid(String token, String username) {
//
//        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
//
//    }
// 
//    private boolean isTokenExpired(String token) {
//
//        return extractExpiration(token).before(new Date());
//
//    }
// 
//    private Date extractExpiration(String token) {
//
//        return extractClaim(token, Claims::getExpiration);
//
//    }
//
//}
//
// 

package com.OTRAS.DemoProject.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", jti);
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .setId(jti)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object idObj = claims.get("id");
        if (idObj == null) {
            idObj = claims.get("userId");
        }
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }
}