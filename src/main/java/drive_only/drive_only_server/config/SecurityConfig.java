    package drive_only.drive_only_server.config;

    import drive_only.drive_only_server.security.JwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.context.annotation.Bean;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    //JWT 방식이기 때문에 세션 기반 CSRF 보호가 필요하지 않아 꺼줌
                    .csrf(AbstractHttpConfigurer::disable)
                    //세션을 아예 만들지 않도록 JWT 토큰 인증은 서버 세션을 사용하지 않음
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    //api/login 경로는 누구나 접근 허용
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/login/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    //사용자 로그인 폼 인증 전에 JWT 필터를 실행 클라이언트가 보낸 JWT 토큰을 검증
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    //SecurityFilterChain 객체를 생성
                    .build();
        }
    }

