package com.jadwal.restfulapi.interceptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadwal.restfulapi.controller.AuthController;
import com.jadwal.restfulapi.controller.TimelineController;
import com.jadwal.restfulapi.controller.UserController;
import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.service.FreeTableService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private FreeTableService freeTableService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();

        // GET selalu diizinkan. OPTIONS dikecualikan supaya CORS preflight tetap
        // sukses; request aslinya (POST/PUT/PATCH/DELETE) yang akan diblok di bawah.
        if (HttpMethod.GET.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        if (handler instanceof HandlerMethod hm) {
            if (hm.getBeanType().equals(AuthController.class)) {
                return true;
            }
            if (hm.getBeanType().equals(UserController.class)) {
                return true;
            }
            if (hm.getBeanType().equals(TimelineController.class)
                    && hm.getMethod().getName().equals("generateComplete")) {
                return true;
            }
        }


        Optional<FreeTable> freeTableOpt = freeTableService.findStatus();
        boolean isGenerating = freeTableOpt.isPresent()
                && Boolean.TRUE.equals(freeTableOpt.get().getIsGenerating());

        if (isGenerating) {
            HTTPCode httpCode = HTTPCode.CONFLICT;
            ErrorMessage error = new ErrorMessage(httpCode,
                    "Generation is In Process, Write Process Are Halted");

            response.setStatus(httpCode.getCode());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return false;
        }

        return true;
    }
}