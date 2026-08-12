package com.jadwal.restfulapi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.SuccessExample;
import com.jadwal.restfulapi.handler.StatusHandler;
import com.jadwal.restfulapi.service.FreeTableService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.FreeTable;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/timeline")
public class TimelineController {

    @Autowired
    private FreeTableService freeTableService;

    @Autowired
    private StatusHandler statusHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Object data = "";

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

    @NoAuth
    @SuccessExample(value = "{\"id\":\"uuid\",\"isGenerating\":true,\"isOdd\":true,\"academicYear\":2026}")
    @ErrorExample(code = "404", name = "not-found", message = "Status Not Found")
    @GetMapping("/generate")
    public ResponseEntity<Object> generate() {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<FreeTable> freeTableOpt = freeTableService.findStatus();
            if (freeTableOpt.isPresent()) {
                FreeTable freeTable = freeTableService.toggleGenerating(freeTableOpt.get());
                Map<String, Object> statusPayload = Map.ofEntries(
                        Map.entry("isGenerating", freeTable.getIsGenerating()),
                        Map.entry("isOdd", freeTable.getIsOdd()),
                        Map.entry("academicYear", freeTable.getAcademicYear()));
                statusHandler.broadcast(objectMapper.writeValueAsString(statusPayload));
                data = statusPayload;
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
