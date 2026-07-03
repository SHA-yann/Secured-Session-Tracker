package com.um.configuration;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;


/**
 * Utility class for creating and clearing HTTP cookies.
 * Primarily used for managing refresh tokens.
 */
@Component
public class CookieProvider {

    /**
     * Creates an HTTP-only cookie.
     *
     * @param name   cookie name
     * @param value  cookie value (e.g., refresh token)
     * @param domain cookie domain
     * @param secure whether the cookie is secure (HTTPS-only)
     * @param maxAge maximum age in seconds
     * @return configured HttpServlet Cookie
     */
    public static ResponseCookie createCookie(String name, String value, String domain, int maxAge) {
        
        return ResponseCookie.from(name,value)
        					.httpOnly(true)
        					.secure(true)
        					.path("/")
        					.maxAge(maxAge)
        					.domain(domain)
        					.sameSite("Lax")
        					.build();
    }

    public static ResponseCookie clearCookie(String name ) {
    	return ResponseCookie.from(name,"")
				.httpOnly(true)
				.secure(false)
				.sameSite("Strict")
				.path("/")
				.maxAge(Duration.ZERO)
				.build();
    }
}
