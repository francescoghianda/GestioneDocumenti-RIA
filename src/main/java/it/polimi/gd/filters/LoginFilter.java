package it.polimi.gd.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "LoginFilter")
public class LoginFilter implements Filter
{

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String requestURI = request.getRequestURI();
        if(requestURI.startsWith("/assets/") || requestURI.startsWith("/register") || requestURI.startsWith("/check-username") || requestURI.startsWith("/check-email"))
        {
            filterChain.doFilter(request, servletResponse);
            return;
        }

        if(request.getSession().getAttribute("user") == null)
        {
            RequestDispatcher dispatcher = request.getRequestDispatcher("/login");
            dispatcher.forward(request, servletResponse);
            return;
        }

        filterChain.doFilter(request, servletResponse);
    }
}
