package com.pettrackerreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("ldw")
                .password(passwordEncoder().encode("ldw"))
                .roles("ADMIN")
                .build();
        
        return new InMemoryUserDetailsManager(admin);
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/favicon-*.ico", "/favicon-*.svg").permitAll()
                .antMatchers("/uploads/**").permitAll() // Allow uploaded files access
                .antMatchers("/", "/blogs/**", "/reviews/**", "/search","/subscription", "/affiliate-disclosure", "/sitemap*.xml", "/robots.txt", "/llmx.txt", "/site.webmanifest","/about-us").permitAll()
                .antMatchers("/0a1530a2305041dbaa781156c2ce4c64.txt").permitAll() // Allow access to Bing verification file
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            .and()
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    String requestURI = request.getRequestURI();
                    if (requestURI.startsWith("/admin/login")) {
                        response.sendRedirect("/admin/login");
                    } else {
                        response.sendRedirect("/");
                    }
                })
            .and()
            .logout()
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            .and()
            .csrf().disable(); // Disable CSRF for simplicity in this demo
    }
}