package com.library.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Equivalent of Node's jsonwebtoken usage:
 *   jwt.sign({ id: admin._id }, "SECRET_KEY", { expiresIn: "7d" })
 */
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration-ms}")
	private long expirationMs;

	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String adminId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.setSubject(adminId)
				.claim("id", adminId)
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(getKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String validateAndGetAdminId(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
}
