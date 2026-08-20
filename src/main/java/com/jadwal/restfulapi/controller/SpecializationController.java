package com.jadwal.restfulapi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.annotation.SuccessExample;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.SpecializationService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.dto.SpecializationDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/specialization")
public class SpecializationController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SpecializationService specializationService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Jaringan Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping
    public ResponseEntity<Object> getSpecializations(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user) || authService.isNtHumAdmin(user)) {
                    List<Specialization> specializations = specializationService.findAllSpecialization();
                    ArrayList<Map<String, Object>> specializationList = new ArrayList<>();
                    for (Specialization specialization : specializations) {
                        specializationList.add(Map.ofEntries(
                                Map.entry("id", specialization.getId()),
                                Map.entry("name", specialization.getName()),
                                Map.entry("createdAt", specialization.getCreatedAt()),
                                Map.entry("updatedAt", specialization.getUpdatedAt())));
                    }
                    data = specializationList;
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Jaringan Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Specialization Not Found")
    @GetMapping("/{specializationId}")
    public ResponseEntity<Object> getSpecializationById(HttpServletRequest request,
            @PathVariable String specializationId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user) || authService.isNtHumAdmin(user)) {
                    Optional<Specialization> specializationOpt = specializationService
                            .findSpecializationById(specializationId);
                    if (specializationOpt.isPresent()) {
                        Specialization specialization = specializationOpt.get();
                        data = Map.ofEntries(
                                Map.entry("id", specialization.getId()),
                                Map.entry("name", specialization.getName()),
                                Map.entry("createdAt", specialization.getCreatedAt()),
                                Map.entry("updatedAt", specialization.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Specialization Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
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

    @SuccessExample(value = "{\"message\":\"Specialization Deleted Successfully\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Specialization Not Found")
    @DeleteMapping("/{specializationId}")
    public ResponseEntity<Object> deleteSpecialization(HttpServletRequest request,
            @PathVariable String specializationId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user) || authService.isNtHumAdmin(user)) {
                    Optional<Specialization> specializationOpt = specializationService
                            .findSpecializationById(specializationId);
                    if (specializationOpt.isPresent()) {
                        Specialization specialization = specializationOpt.get();
                        specializationService.deleteSpecialization(specialization);
                        data = Map.ofEntries(
                                Map.entry("message", "Specialization Deleted Successfully"));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Specialization Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Jaringan Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PostMapping
    public ResponseEntity<Object> createSpecialization(HttpServletRequest request,
            @RequestBody SpecializationDTO specializationDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            specializationDTO.checkDTO();
            if (specializationService.isSpecializationExistByName(specializationDTO.getName()))
                throw new IllegalArgumentException("Name Already Exist");

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user) || authService.isNtHumAdmin(user)) {
                    Specialization createdSpecialization = specializationService.createSpecialization(specializationDTO,
                            user);
                    data = Map.ofEntries(
                            Map.entry("id", createdSpecialization.getId()),
                            Map.entry("name", createdSpecialization.getName()),
                            Map.entry("createdAt", createdSpecialization.getCreatedAt()),
                            Map.entry("updatedAt", createdSpecialization.getUpdatedAt()));
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Jaringan Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Specialization Not Found")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PutMapping("/{specializationId}")
    public ResponseEntity<Object> editSpecialization(HttpServletRequest request,
            @RequestBody SpecializationDTO specializationDTO,
            @PathVariable String specializationId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            specializationDTO.checkDTO();

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user) || authService.isNtHumAdmin(user)) {
                    Optional<Specialization> editedSpecializationOpt = specializationService
                            .findSpecializationById(specializationId);
                    if (editedSpecializationOpt.isPresent()) {
                        Specialization editedSpecialization = editedSpecializationOpt.get();
                        if (specializationService.isSpecializationExistByNameAndIdIsNot(specializationDTO.getName(),
                                editedSpecialization.getId()))
                            throw new IllegalArgumentException("Name Already Exist");

                        editedSpecialization = specializationService.editSpecialization(editedSpecialization,
                                specializationDTO,
                                user);
                        data = Map.ofEntries(
                                Map.entry("id", editedSpecialization.getId()),
                                Map.entry("name", editedSpecialization.getName()),
                                Map.entry("createdAt", editedSpecialization.getCreatedAt()),
                                Map.entry("updatedAt", editedSpecialization.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Specialization Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
            } else {
                httpCode = HTTPCode.UNAUTHORIZED;
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
