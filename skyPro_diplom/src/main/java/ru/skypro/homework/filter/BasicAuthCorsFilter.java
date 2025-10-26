package ru.skypro.homework.filter;


import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class BasicAuthCorsFilter extends OncePerRequestFilter {

    /**
     * Обрабатывает каждый запрос, добавляя заголовок "Access-Control-Allow-Credentials" для поддержки CORS.
     *
     * @param httpServletRequest  Входящий HTTP-запрос.
     * @param httpServletResponse Исходящий HTTP-ответ.
     * @param filterChain         Цепочка фильтров для дальнейшей обработки запроса.
     * @throws ServletException В случае ошибки при обработке запроса.
     * @throws IOException      В случае ошибки ввода-вывода.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
