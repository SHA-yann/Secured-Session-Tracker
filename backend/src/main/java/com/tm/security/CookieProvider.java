package com.tm.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;

@Component
public class CookieProvider {

	public static Cookie createCookie(String name,String rtoken, String domain, boolean secure,int mAs) {
		
		 Cookie cook = new Cookie(name, rtoken);
				cook.setHttpOnly(true);
				cook.setSecure(secure);
				cook.setPath("/");
				cook.setMaxAge(mAs);
				cook.setDomain(domain);
		return cook;
	}
	
	public static Cookie clearCookie(String name, String domain, boolean secure) {
		
		Cookie cook = new Cookie(name, "");
				cook.setHttpOnly(true);
				cook.setSecure(secure);
				cook.setPath("/");
				cook.setMaxAge(0);
				cook.setDomain(domain);
		return cook;
							
							
	}
}
