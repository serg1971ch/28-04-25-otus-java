package ru.otus.minioBot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer; // Импорт Customizer

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((authorize) -> authorize
//                        // Разрешаем доступ без аутентификации ко всем запросам, начинающимся с /app/
//                        .requestMatchers("/app/**").permitAll()
//                        // Все остальные запросы требуют аутентификации
//                        .anyRequest().authenticated()
//                )
//                // Отключаем CSRF для упрощения, но в продакшене это не рекомендуется!
//                .csrf(csrf -> csrf.disable()) // или .csrf(Customizer.withDefaults()) если нужен дефолтный
//                // Форма логина, если вы хотите иметь ее
//                .formLogin(Customizer.withDefaults()); // или .formLogin(login -> login.disable()) если не нужна
//
//        return http.build();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        // Разрешаем доступ без аутентификации к статическим ресурсам
                        .requestMatchers(
                                "/css/**",           // Для CSS файлов
                                "/js/**",            // Для JavaScript файлов
                                "/images/**",        // Для изображений
                                "/webjars/**",       // Если используете WebJars (например, Bootstrap, jQuery)
                                "/favicon.ico",      // Иконка сайта
                                "/static/**",
                                "/photos/**"
                        ).permitAll()
                        // Разрешаем доступ без аутентификации ко всем запросам, начинающимся с /app/
                        .requestMatchers("/app/**").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Отключаем CSRF для упрощения, но в продакшене это не рекомендуется!
                .csrf(csrf -> csrf.disable())
                // Форма логина, если вы хотите иметь ее
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    // Если вам не нужен Basic Authentication, вы можете удалить этот метод или настроить его
    // Если у вас нет пользователя по умолчанию, Spring Security сгенерирует его в логах.
    // Если вы хотите простой пользователь, можете добавить такой бин:
    /*
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder() // НЕ использовать в продакшене!
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
    */
}
