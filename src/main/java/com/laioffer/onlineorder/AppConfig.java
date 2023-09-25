package com.laioffer.onlineorder;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setCreateUserSql("INSERT INTO customers (email, password, enabled) VALUES (?,?,?)");
        userDetailsManager.setCreateAuthoritySql("INSERT INTO authorities (email, authority) values (?,?)");
        userDetailsManager.setUsersByUsernameQuery("SELECT email, password, enabled FROM customers WHERE email = ?");
        userDetailsManager.setAuthoritiesByUsernameQuery("SELECT email, authorities FROM authorities WHERE email = ?");
        return userDetailsManager;
    }

    @Bean//PasswordEncoder把存到db的password单向加密防止db被窃取用户数据
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()//cross site request forgery 防止前后端端口不一样有坏人access, disable掉这个防范机制因为目前前后端端口不一样
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()//前端
                                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.json", "/*.png", "/static/**").permitAll()//前端需要public可见
                                .requestMatchers(HttpMethod.POST, "/login", "/logout", "/signup").permitAll()//没登陆时也可以call login endpoint
                                .requestMatchers(HttpMethod.GET, "/restaurants/**", "/restaurant/**").permitAll()//用户没登录时也要能看到restaurant下的内容
                                .anyRequest().authenticated()//其他任何都需要authentication，这么写更安全
                ) //以下都是customize，
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))//unauthenticated进来后显示unauthorized不去spring boot自带的login页面
                .and()
                .formLogin()//session-based authentication
                .successHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                .failureHandler(new SimpleUrlAuthenticationFailureHandler())
                .and()
                .logout()
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK));//return OK,不要跳转去login
        return http.build();
    }

}
