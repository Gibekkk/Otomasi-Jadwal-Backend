package com.moodbites.restfulapi.controller;

import com.moodbites.restfulapi.service.FormService;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moodbites.restfulapi.util.ErrorMessage;
import com.moodbites.restfulapi.util.HTTPCode;

import jakarta.servlet.http.HttpServletRequest;

import com.moodbites.restfulapi.service.AuthService;
// import com.moodbites.restfulapi.service.ExternalService;
import com.moodbites.restfulapi.model.Session;
import com.moodbites.restfulapi.model.User;
import com.moodbites.restfulapi.model.enums.Mood;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/external")
public class ExternalController {

    @Autowired
    private AuthService authService;

    @Autowired
    private FormService formService;

    // @Autowired
    // private ExternalService externalService;

    private static final String MOOD_WEBHOOK =
    "https://n8n.moodbites.qzz.io/webhook/2a7da476-e524-4fd1-972a-6e50ce894cbd";
 
private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(3))
    .build();


    private Object data = "";

    @GetMapping("/{mood}/{userId}")
public ResponseEntity<Object> getPreferences(HttpServletRequest request,
        @PathVariable String mood,
        @PathVariable String userId) {
 
    HTTPCode httpCode = HTTPCode.OK;
    try {
        if (Mood.checkExist(mood)) {
            Mood moodEnum = Mood.fromString(mood);
            Optional<User> userOpt = authService.findUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                data = formService.getPreferenceByMoodAndUser(moodEnum, user);
            } else {
                httpCode = HTTPCode.NOT_FOUND;
                data = new ErrorMessage(httpCode, "User not found");
            }
        } else {
            httpCode = HTTPCode.NOT_FOUND;
            data = new ErrorMessage(httpCode, "Mood not found");
        }
    } catch (IllegalArgumentException e) {
        httpCode = HTTPCode.BAD_REQUEST;
        data = new ErrorMessage(httpCode, e.getMessage());
    } catch (Exception e) {
        httpCode = HTTPCode.INTERNAL_SERVER_ERROR;
        data = new ErrorMessage(httpCode, e.getMessage());
    }
 
    // ── Kirim ke webhook n8n sebelum return ───────────────────────────────────
    try {
        String payload = String.format(
            "{\"user_id\":\"%s\",\"mood\":\"%s\"}", userId, mood);
 
        HttpRequest webhookReq = HttpRequest.newBuilder()
            .uri(URI.create(MOOD_WEBHOOK))
            .timeout(Duration.ofSeconds(5))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
 
        HTTP_CLIENT.send(webhookReq, HttpResponse.BodyHandlers.discarding());
    } catch (Exception e) {
        System.err.println("[Webhook] Failed: " + e.getMessage());
    }
    // ─────────────────────────────────────────────────────────────────────────
 
    return ResponseEntity
            .status(httpCode.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(data);
}


    // @GetMapping("/recommendations/{mood}")
    // public ResponseEntity<Object> getRecommendations(HttpServletRequest request, @PathVariable String mood) {
    //     String sessionToken = request.getHeader("Token");
    //     HTTPCode httpCode = HTTPCode.OK;
    //     try {
    //         if (Mood.checkExist(mood)) {
    //             Mood moodEnum = Mood.fromString(mood);
    //             Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
    //             if (sessionOpt.isPresent()) {
    //                 Session session = sessionOpt.get();
    //                 data = externalService.getRecommendations(session.getUserId().getId(), mood);
    //                 // data = externalService.getRecommendations(formService.getPreferenceByMoodAndUser(moodEnum, session.getUserId()));
    //             } else {
    //                 httpCode = HTTPCode.FORBIDDEN;
    //                 data = new ErrorMessage(httpCode, "Authentication Failed");
    //             }
    //         } else {
    //             httpCode = HTTPCode.NOT_FOUND;
    //             data = new ErrorMessage(httpCode, "Mood not found");
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
