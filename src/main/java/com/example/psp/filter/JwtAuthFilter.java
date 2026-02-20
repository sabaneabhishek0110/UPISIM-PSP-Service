package com.example.psp.filter;

import com.example.psp.service.PSPService;
import com.example.psp.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService TokenService;
    private final PSPService pspService;

    public JwtAuthFilter(TokenService TokenService,PSPService pspService) {
        this.TokenService = TokenService;
        this.pspService = pspService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String path = request.getRequestURI();

        System.out.println("Entered Into filter : "+path);

        if (path.startsWith("/api/user/login") ||
                path.startsWith("/api/user/register") ||
                path.startsWith("/health")) {

            filterChain.doFilter(request, response);
            System.out.println("Passed through filter : "+path);
            return;
        }

        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String userId = TokenService.validateToken(token);

                if(!pspService.userExits(userId)){
                    throw new Exception("User does not exist");
                }
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, null);
                String CurrUserVpa = pspService.getUserVpa(userId);
                System.out.println("CurrUserVpa : "+CurrUserVpa);
                request.setAttribute("vpa", CurrUserVpa);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                System.out.println("JWT Parsing Error : " + e.getMessage());
            }
        }


        filterChain.doFilter(request, response);
        System.out.println("Completed Filter : "+path);
    }

    private String extractToken(HttpServletRequest request) {
        // Prefer Token in Cookie (HttpOnly Cookie Strategy)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("token")) {
                    return c.getValue();
                }
            }
        }

        // fallback if token passed in header (Postman or mobile app)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
