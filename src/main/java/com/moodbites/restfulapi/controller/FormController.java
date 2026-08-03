package com.moodbites.restfulapi.controller;

import com.moodbites.restfulapi.service.FormService;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moodbites.restfulapi.util.ErrorMessage;
import com.moodbites.restfulapi.util.HTTPCode;

import jakarta.servlet.http.HttpServletRequest;

import com.moodbites.restfulapi.service.AuthService;
import com.moodbites.restfulapi.dto.MoodFormDTO;
import com.moodbites.restfulapi.model.Session;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/form")
public class FormController {

    @Autowired
    private AuthService authService;

    @Autowired
    private FormService formService;

    private Object data = "";

    @PostMapping
    public ResponseEntity<Object> editPreferences(HttpServletRequest request, @RequestBody MoodFormDTO moodFormDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            moodFormDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                formService.updateUserPreferences(session.getUserId(), moodFormDTO);
                data = Map.of(
                        "userId", session.getUserId().getId(),
                        "message", "Preferences updated successfully");
            } else {
                httpCode = HTTPCode.FORBIDDEN;
                data = new ErrorMessage(httpCode, "Authentication Failed");
            }
        } catch (IllegalArgumentException e) {
            httpCode = HTTPCode.BAD_REQUEST;
            data = new ErrorMessage(httpCode, e.getMessage());
        } catch (Exception e) {
            httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
            data = new ErrorMessage(httpCode, e.getMessage());
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    // @GetMapping
    // public ResponseEntity<Object> getPreferences(HttpServletRequest request) {
    //     String sessionToken = request.getHeader("Token");
    //     HTTPCode httpCode = HTTPCode.OK;
    //     try {
    //         Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
    //         if (sessionOpt.isPresent()) {
    //             Session session = sessionOpt.get();
    //             // formService.updateUserPreferences(session.getUserId(), moodFormDTO);
    //             data = Map.of(
    //                     "userId", session.getUserId().getId(),
    //                     "message", "Preferences updated successfully");
    //         } else {
    //             httpCode = HTTPCode.FORBIDDEN;
    //             data = new ErrorMessage(httpCode, "Authentication Failed");
    //         }
    //     } catch (IllegalArgumentException e) {
    //         httpCode = HTTPCode.BAD_REQUEST;
    //         data = new ErrorMessage(httpCode, e.getMessage());
    //     } catch (Exception e) {
    //         httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
    //         data = new ErrorMessage(httpCode, e.getMessage());
    //     }

    //     return ResponseEntity
    //             .status(httpCode.getStatus())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .body(data);
    // }
}
