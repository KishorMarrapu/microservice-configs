package com.OTRAS.DemoProject.Security;
//package com.OTRAS.DemoProject.Security;
 
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
 
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS here
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers("/Answerkey/**",
            				"/api/jobpost/**",
            				"/api/syllabus/**",
            				"/api/auth/**",
            				"/api/candidate/**",
            				"/api/answerkey/**",
            				"/api/cutoff/**",
            				"/governmentAdmitCard/**",
            				"/api/pqp/**",
            				"/api/result/**",
            				"/api/Apply/**",
            				"/api/digilocker/**",
            				"/api/admit-card/**",
            				"/api/Exam/**",
            				"/api/question-paper/**",
            				"/api/examAssignment/**",
            				"/api/payment/**"
            				).permitAll()
            		.requestMatchers("/api/protected/**").authenticated()
                    .anyRequest().authenticated()
                )
                .addFilterBefore(
                    jwtAuthenticationFilter,
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );
            
 
        return http.build();
    }
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
 
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

  
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5171",
            "http://localhost:5172", 
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            
            "https://front-end-exam-alpha.vercel.app",
            "https://front-end-admin-aw6p.vercel.app",
            "https://front-end-user-five.vercel.app",
            	 
            
            "https://otr-admin-diwkz6a1n-otrass-projects.vercel.app",
            "https://*.vercel.app", //  all Vercel subdomains
            "http://localhost:*", //  all localhost ports
            "https://*" //  all HTTPS domains (for testing)
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
