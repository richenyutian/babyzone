package com.babysteps.config;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AppDataProperties properties;

    public AuthInterceptor(AppDataProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        boolean authenticated = session != null
                && Boolean.TRUE.equals(session.getAttribute(properties.getSessionAttribute()));
        if (authenticated) {
            return true;
        }

        String accept = request.getHeader("Accept");
        boolean expectsHtml = accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
        if (expectsHtml && !request.getRequestURI().startsWith("/api/")) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"请先登录后台。\"}");
        } else {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("请先登录后台。");
        }
        return false;
    }
}
