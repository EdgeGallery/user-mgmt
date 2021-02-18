/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.user.auth.config.security;

import java.util.Arrays;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.config.filter.GuestUserAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] ADMIN_ROLES = {
        "APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "ATP_ADMIN", "LAB_ADMIN"
    };

    @Value("${cors.allow.origins}")
    private String allowOrigins;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private MecUserDetailsService mecUserDetailsService;

    @Autowired
    private LoginFailHandler loginFailHandler;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.exceptionHandling()
            .authenticationEntryPoint(new OAuthUnauthorizedEntryPoint("/#/", smsConfig.getEnabled()))
            .and()
            .authorizeRequests()
            .antMatchers("/", "/oauth/**", "/login", "/css/**", "/fonts/**", "/img/**",
                "/js/**", "/favicon.ico",
                "/index.html", "/user-privacy.md", "/user-agreement.md")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/v1/users", "/v1/users/action/uniqueness", "/v1/identity/sms")
            .permitAll()
            .antMatchers(HttpMethod.PUT, "/v1/users/password")
            .permitAll()
            .antMatchers(HttpMethod.GET, "/health")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/v1/users/list")
            .hasAnyRole(ADMIN_ROLES)
            .antMatchers(HttpMethod.PUT, "/v1/users/status/**", "/v1/users/settings/**")
            .hasAnyRole(ADMIN_ROLES)
            .antMatchers(HttpMethod.PUT, "/v1/users/**")
            .hasAnyRole(ADMIN_ROLES)
            .antMatchers(HttpMethod.DELETE, "/v1/users/**")
            .hasAnyRole(ADMIN_ROLES)
            .antMatchers(HttpMethod.GET, "/auth/login-info")
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .loginPage("/#/")
            .loginProcessingUrl("/login")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailHandler)
            .and()
            .cors().and().csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        httpSecurity.addFilterAfter(guestAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(mecUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    GuestUserAuthenticationFilter guestAuthenticationFilter() throws Exception {
        GuestUserAuthenticationFilter filter = new GuestUserAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailHandler);
        return filter;
    }

    /**
     * CorsFilter solve cross-domain issues for logout api.
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList(allowOrigins.split(",")));
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("GET");
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/auth/logout", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    /**
     * Define the PBKDF2 encoder with sha256.
     *
     * @return
     */
    @Bean
    public Pbkdf2PasswordEncoder passwordEncoder() {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
        encoder.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        return encoder;
    }
}