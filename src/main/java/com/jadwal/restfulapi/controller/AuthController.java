package com.jadwal.restfulapi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jadwal.restfulapi.dto.LoginDTO;
import com.jadwal.restfulapi.dto.ProfileDTO;
import com.jadwal.restfulapi.dto.RegisterDTO;
import com.jadwal.restfulapi.dto.VerifyOTPDTO;
import com.jadwal.restfulapi.service.EmailService;
import com.jadwal.restfulapi.service.FormService;
import com.jadwal.restfulapi.service.OTPService;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.OTP;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private FormService formService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    private Object data = "";

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDTO loginDTO) {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            loginDTO.checkDTO();
            Optional<Session> sessionOpt = authService.authenticateUser(loginDTO.getEmail(), loginDTO.getPassword(),
                    loginDTO.getFcmToken());
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                data = Map.of(
                        "userId", session.getUserId().getId(),
                        "isFinishedForm", session.getUserId().getEditedPreferenceAt() != null,
                        "token", session.getToken());
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
                data = new ErrorMessage(httpCode, "Email or Password is Incorrect");
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

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterDTO registerDTO) {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            registerDTO.checkDTO();
            if (authService.isEmailAvailable(registerDTO.getEmail())) {
                User newUser = authService.registerUser(registerDTO.getEmail(), registerDTO.getPassword(),
                        registerDTO.getFcmToken(), registerDTO.getName());
                OTP newOTP = otpService.generateOTP(newUser);
                emailService.sendOTPRegisToLogin(newUser, newOTP);
                data = Map.of(
                        "userId", newUser.getId(),
                        "otpValidUntil", newOTP.getValidUntil());
            } else {
                httpCode = HTTPCode.CONFLICT;
                data = new ErrorMessage(httpCode, "Email has been registered");
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

    @PostMapping("/register/verify")
    public ResponseEntity<Object> registerVerify(@RequestBody VerifyOTPDTO verifyOTPDTO) {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            verifyOTPDTO.checkDTO();
            Optional<User> userOpt = otpService.verifyOTP(verifyOTPDTO.getLoginId(), verifyOTPDTO.getCode());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Session session = authService.verifyUser(user);
                formService.createUserPreferences(user);
                data = Map.of(
                        "userId", session.getUserId().getId(),
                        "token", session.getToken());
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
                data = new ErrorMessage(httpCode, "OTP Not Valid or Has Expired");
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

    @PostMapping("/refreshOtp/{userId}")
    public ResponseEntity<Object> refreshOtp(@PathVariable String userId) {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<User> userOpt = authService.findUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Optional<OTP> otpOpt = otpService.refreshOTP(user);
                if (otpOpt.isPresent()) {
                    OTP otp = otpOpt.get();
                    emailService.sendOTPRegisToLogin(user, otp);
                    data = Map.of(
                            "userId", user.getId(),
                            "otpValidUntil", otp.getValidUntil());
                } else {
                    httpCode = HTTPCode.NOT_FOUND;
                    data = new ErrorMessage(httpCode, "OTP Not Found");
                }

            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
                data = new ErrorMessage(httpCode, "Login ID Not Found");
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

    @GetMapping("/profile")
    public ResponseEntity<Object> getProfile(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                data = Map.of(
                        "id", session.getUserId().getId(),
                        "email", session.getUserId().getEmail(),
                        "joinedYear", session.getUserId().getCreatedAt().getYear(),
                        "name", session.getUserId().getName());
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

    @PatchMapping("/profile")
    public ResponseEntity<Object> editProfile(HttpServletRequest request, @RequestBody ProfileDTO profileDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            profileDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = authService.editUserProfile(session.getUserId(), profileDTO);
                data = Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "joinedYear", user.getCreatedAt().getYear(),
                        "name", user.getName());
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

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                authService.deleteSession(session);
                data = Map.of(
                        "message", "Logout Successful");
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

    @GetMapping("/check")
    public ResponseEntity<Object> checkToken(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                data = Map.of(
                        "id", session.getUserId().getId(),
                        "name", session.getUserId().getName(),
                        "isFinishedForm", session.getUserId().getEditedPreferenceAt() != null);
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
}
