package com.ab.tasktracker.security;

import com.ab.tasktracker.constants.TaskTrackerConstants;
import com.ab.tasktracker.entity.User;
import com.ab.tasktracker.helper.UserHelper;
import com.ab.tasktracker.security.filter.AuthenticationFilter;
import com.ab.tasktracker.security.filter.XSSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;
    @Autowired
    private XSSFilter xssFilter;
    @Autowired
    private UserHelper userHelper;

    String[] Whitelisted_URLS = {"**/signup", "**/login", "**/twofactorsignup", "**/api", "/swagger-ui/**", "/v3/api-docs/**", "/v2/api-docs/**", "/v1/api-docs/**", "**/forgot-password", "**/validate-otp", "**/test/**", "**/actuator/health"};

    @Value("${server.host}")
    private String hostName;

    /**
     * Here we add Whitelisted URLS, these requests will bypass all security filters, including
     * authentication and authorization checks.
     *
     * @return WebSecurityCustomizer
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(Whitelisted_URLS).and().ignoring().requestMatchers(HttpMethod.POST).and().ignoring().requestMatchers(HttpMethod.GET);
    }


    /**
     * Creates Bean of SecurityFilterChain in which we configure XSS filter, Auth Filter, CORS protection
     * And set Session Management to Stateless hence context holder does not hold authentication object
     *
     * @param httpSecurity HttpSecurity Class
     *                     <p>
     *                     /health
     *                     /task/**
     *                     anyRequest()
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable).
                cors(Customizer.withDefaults()).
                authorizeHttpRequests(auth -> auth.anyRequest().authenticated()).
                addFilterBefore(xssFilter, UsernamePasswordAuthenticationFilter.class).
                headers(headersConfigurer ->
                        headersConfigurer.contentSecurityPolicy(
                                csp -> csp.policyDirectives(String.format("script-src 'self' %s;", hostName))
                        )
                ).
                sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).
                addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://xyz.com")); // Allow requests from xyz.com
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of(TaskTrackerConstants.AUTH_HEADER, "DeviceId"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Registers our Filter and disables it hence not called everytime only for non-whitelisted URLS
     *
     * @param filter FilterRegistrationBean
     * @return
     */
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> registration(AuthenticationFilter filter) {
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<XSSFilter> registrationXSS(XSSFilter filter) {
        FilterRegistrationBean<XSSFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User userEntity = userHelper.getUserDetails(username);
            if (userEntity == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return userEntity;
        };
    }

    /**
     * Authenticates based on user details.
     *
     * @return AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Using BCrypt to encode passwords
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
