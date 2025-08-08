package com.job.security;

import com.job.entity.Employer;
import com.job.entity.User;
import com.job.repository.EmployerRepository;
import com.job.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No token found in header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("📦 JWT received: " + token);

        String username = jwtUtil.extractUsername(token);
        if (username == null) {
            System.out.println("❌ Could not extract username from token");
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            System.out.println("❌ No user found with username: " + username);
            filterChain.doFilter(request, response);
            return;
        }

        User user = optionalUser.get();

        // ✅ Set Spring Security context with role
        String roleName = "ROLE_" + user.getRole().name(); // e.g., ROLE_EMPLOYER
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        System.out.println("🔐 Spring Security context set for user: " + username + " with role: " + roleName);

        // ✅ Inject specific user type into request attribute
        if (user.getRole().name().equals("EMPLOYER")) {
            Optional<Employer> optionalEmployer = employerRepository.findById(user.getId());
            if (optionalEmployer.isEmpty()) {
                System.out.println("❌ Employer not found for ID: " + user.getId());
                filterChain.doFilter(request, response);
                return;
            }
            Employer employer = optionalEmployer.get();
            request.setAttribute("user", employer);
            System.out.println("✅ Injected Employer user: " + employer.getUsername());
        } else {
            request.setAttribute("user", user);
            System.out.println("✅ Injected JobSeeker user: " + user.getUsername());
        }

        filterChain.doFilter(request, response);
    }
}
