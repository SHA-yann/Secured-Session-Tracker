package com.tm.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;

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
    public static Cookie createCookie(String name, String value, String domain, boolean secure, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain(domain);
        return cookie;
    }

    /**
     * Clears a cookie by setting its value to empty and max age to 0.
     *
     * @param name   cookie name
     * @param domain cookie domain
     * @param secure whether the cookie is secure (HTTPS-only)
     * @return cleared HttpServlet Cookie
     */
    public static Cookie clearCookie(String name, String domain, boolean secure) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0); // expire immediately
        cookie.setDomain(domain);
        return cookie;
    }
}
