package com.library.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Equivalent of Node's bcryptjs usage: bcrypt.genSalt + bcrypt.hash / bcrypt.compare
 */
@Component
public class PasswordUtil {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

	public String hash(String plainPassword) {
		return encoder.encode(plainPassword);
	}

	public boolean matches(String plainPassword, String hashedPassword) {
		if (plainPassword == null || hashedPassword == null) return false;
		return encoder.matches(plainPassword, hashedPassword);
	}
}
