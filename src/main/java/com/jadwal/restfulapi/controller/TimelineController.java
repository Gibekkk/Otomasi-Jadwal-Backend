package com.jadwal.restfulapi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.SuccessExample;
import com.jadwal.restfulapi.dto.GenerateDTO;
import com.jadwal.restfulapi.handler.StatusHandler;
import com.jadwal.restfulapi.service.FreeTableService;
import com.jadwal.restfulapi.service.ScheduleService;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import jakarta.servlet.http.HttpServletRequest;

import com.jadwal.restfulapi.model.FreeTable;
import com.jadwal.restfulapi.model.Schedule;
import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/timeline")
public class TimelineController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private FreeTableService freeTableService;

    @Autowired
    private AuthService authService;

    @Autowired
    private StatusHandler statusHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Object data = "";

    @NoAuth
    @SuccessExample(value = "[{\"id\":\"uuid\",\"timeStart\":\"7.30\",\"timeEnd\":\"8.20\"}]")
    @ErrorExample(code = "404", name = "not-found", message = "Status Not Found")
    @GetMapping("/schedules")
    public ResponseEntity<Object> getShcedules() {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            ArrayList<Object> scheduleList = new ArrayList<Object>();
            for (Schedule schedule : scheduleService.findAllSchedule()) {
                scheduleList.add(Map.ofEntries(
                        Map.entry("id", schedule.getId()),
                        Map.entry("timeStart", schedule.getTimeStart()),
                        Map.entry("timeEnd", schedule.getTimeEnd())));
            }
            data = scheduleList;
        } catch (Exception e) {
            httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
            data = new ErrorMessage(httpCode, e.getMessage());
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @NoAuth
    @SuccessExample(value = "{\"id\":\"uuid\",\"isGenerating\":false,\"isOdd\":true,\"academicYear\":2026}")
    @ErrorExample(code = "404", name = "not-found", message = "Status Not Found")
    @GetMapping("/status")
    public ResponseEntity<Object> getStatus() {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<FreeTable> freeTableOpt = freeTableService.findStatus();
            if (freeTableOpt.isPresent()) {
                FreeTable freeTable = freeTableOpt.get();
                data = Map.ofEntries(
                        Map.entry("isGenerating", freeTable.getIsGenerating()),
                        Map.entry("isOdd", freeTable.getIsOdd()),
                        Map.entry("academicYear", freeTable.getAcademicYear()));
            } else {
                httpCode = HTTPCode.NOT_FOUND;
                data = new ErrorMessage(httpCode, "Status Not Found");
            }
        } catch (Exception e) {
            httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
            data = new ErrorMessage(httpCode, e.getMessage());
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @SuccessExample(value = "{\"isGenerating\":true,\"isOdd\":true,\"academicYear\":2026}")
    @ErrorExample(code = "404", name = "not-found", message = "Status Not Found")
    @PostMapping("/generate")
    public ResponseEntity<Object> generate(HttpServletRequest request, @RequestBody GenerateDTO generateDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)) {
                    Optional<FreeTable> freeTableOpt = freeTableService.findStatus();
                    if (freeTableOpt.isPresent()) {
                        if (!freeTableOpt.get().getIsGenerating()) {
                            FreeTable freeTable = freeTableService.startGenerating(freeTableOpt.get());
                            Map<String, Object> statusPayload = Map.ofEntries(
                                    Map.entry("isGenerating", freeTable.getIsGenerating()),
                                    Map.entry("isOdd", freeTable.getIsOdd()),
                                    Map.entry("academicYear", freeTable.getAcademicYear()));
                            statusHandler.broadcast(objectMapper.writeValueAsString(statusPayload));
                            data = statusPayload;
                        } else {
                            httpCode = HTTPCode.FORBIDDEN;
                            data = new ErrorMessage(httpCode, "Generation In Process");
                        }
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Status Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
                data = new ErrorMessage(httpCode, "Authentication Failed");
            }
        } catch (Exception e) {
            httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
            data = new ErrorMessage(httpCode, e.getMessage());
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @SuccessExample(value = "{\"isGenerating\":true,\"isOdd\":true,\"academicYear\":2026}")
    @ErrorExample(code = "404", name = "not-found", message = "Status Not Found")
    @PostMapping("/generateComplete")
    public ResponseEntity<Object> generateComplete(@RequestBody String secretKey) {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<FreeTable> freeTableOpt = freeTableService.findStatus();
            if (freeTableOpt.isPresent()) {
                if (freeTableOpt.get().getIsGenerating()) {
                    if (freeTableOpt.get().getSecretKey().equals(secretKey)) {
                        FreeTable freeTable = freeTableService.stopGenerating(freeTableOpt.get());
                        Map<String, Object> statusPayload = Map.ofEntries(
                                Map.entry("isGenerating", freeTable.getIsGenerating()),
                                Map.entry("isOdd", freeTable.getIsOdd()),
                                Map.entry("academicYear", freeTable.getAcademicYear()));
                        statusHandler.broadcast(objectMapper.writeValueAsString(statusPayload));
                        data = statusPayload;
                    } else {
                        httpCode = HTTPCode.UNAUTHORIZED;
                        data = new ErrorMessage(httpCode, "Key Invalid");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Generation Is Not Running");
                }
            } else {
                httpCode = HTTPCode.NOT_FOUND;
                data = new ErrorMessage(httpCode, "Status Not Found");
            }
        } catch (Exception e) {
            httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
            data = new ErrorMessage(httpCode, e.getMessage());
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}
