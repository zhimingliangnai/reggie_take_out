package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. 获取本次请求的URI
        String requestURI = request.getRequestURI();

        // 定义不需要处理的请求路径
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };


        // 2. 判断本次请求是否需要处理
        boolean check = check(uris, requestURI);
        Long empId = (Long) request.getSession().getAttribute("employee");
        Long userId = (Long) request.getSession().getAttribute("user");
        if (check) {
            // 3. 如果不需要处理，则直接放行
            filterChain.doFilter(request, response);
            return;
        }
        // 4.1 判断登录状态，如果已登录，则直接放行
        if (empId != null) {
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }

        // 4.2 判断登录状态，如果以登录，则直接放行
        if (userId != null) {
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }

        /*if (check || empId != null || userId != null) {
            log.info("判断用户是否登录：check = {}, empId = {}, userId = {}", check, empId, userId);
            BaseContext.setCurrentId(empId);
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }*/


        // 5. 如果未登录则返回未登录结果
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param uris
     * @param requestUri
     * @return
     */
    private boolean check(String[] uris, String requestUri) {
        for (String uri : uris) {
            if (PATH_MATCHER.match(uri, requestUri)) {
                return true;
            }
        }
        return false;
    }
}

