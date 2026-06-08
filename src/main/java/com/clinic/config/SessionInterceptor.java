package com.clinic.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts every request.
 * - If the user is not logged in and tries to access a protected route → redirects to /login
 * - If the user is logged in but is not ADMIN and tries to access /admin/** → redirects to /dashboard
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    // Routes that do NOT require login
    private static final String[] PUBLIC_URLS = {
            "/login", "/register", "/", "/css", "/js", "/images"
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();

        // Allow public URLs
        for (String pub : PUBLIC_URLS) {
            if (path.equals(pub) || path.startsWith(pub + "/")) {
                return true;
            }
        }

        HttpSession session = request.getSession(false);
        Object user = (session != null) ? session.getAttribute("user") : null;

        // Not logged in → redirect to login
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Logged in but not ADMIN → block admin routes
        if (path.startsWith("/admin")) {
            com.clinic.entity.User loggedIn = (com.clinic.entity.User) user;
            if (!"ADMIN".equals(loggedIn.getRole())) {
                response.sendRedirect("/dashboard?accessDenied=true");
                return false;
            }
        }

        return true;
    }
}
