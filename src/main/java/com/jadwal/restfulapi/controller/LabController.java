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
import com.jadwal.restfulapi.service.RoomService;
import com.jadwal.restfulapi.service.SpecializationService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.LabGroup;
import com.jadwal.restfulapi.dto.LabGroupDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/lab")
public class LabController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SpecializationService specializationService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Lab Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping
    public ResponseEntity<Object> getLabGroups(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isPmAdmin(user)) {
                    List<LabGroup> labGroups = roomService.findAllLabGroup();
                    ArrayList<Map<String, Object>> labGroupList = new ArrayList<>();
                    for (LabGroup labGroup : labGroups) {
                        labGroupList.add(Map.ofEntries(
                                Map.entry("id", labGroup.getId()),
                                Map.entry("name", labGroup.getName()),
                                Map.entry("specializations", labGroup.getLabSpecializations().stream()
                                        .map(ls -> ls.getSpecializationId().getName())
                                        .collect(Collectors.toList())),
                                Map.entry("createdAt", labGroup.getCreatedAt()),
                                Map.entry("updatedAt", labGroup.getUpdatedAt())));
                    }
                    data = labGroupList;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Lab Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "LabGroup Not Found")
    @GetMapping("/{labGroupId}")
    public ResponseEntity<Object> getLabGroupById(HttpServletRequest request, @PathVariable String labGroupId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<LabGroup> labGroupOpt = roomService.findLabGroupById(labGroupId);
                    if (labGroupOpt.isPresent()) {
                        LabGroup labGroup = labGroupOpt.get();
                        data = Map.ofEntries(
                                Map.entry("id", labGroup.getId()),
                                Map.entry("name", labGroup.getName()),
                                Map.entry("specializations", labGroup.getLabSpecializations().stream()
                                        .map(ls -> ls.getSpecializationId().getName())
                                        .collect(Collectors.toList())),
                                Map.entry("createdAt", labGroup.getCreatedAt()),
                                Map.entry("updatedAt", labGroup.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lab Group Not Found");
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

    @SuccessExample(value = "{\"message\":\"Lab Group Deleted Successfully\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "LabGroup Not Found")
    @DeleteMapping("/{labGroupId}")
    public ResponseEntity<Object> deleteLabGroup(HttpServletRequest request, @PathVariable String labGroupId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<LabGroup> labGroupOpt = roomService.findLabGroupById(labGroupId);
                    if (labGroupOpt.isPresent()) {
                        LabGroup labGroup = labGroupOpt.get();
                        roomService.deleteLabGroup(labGroup);
                        data = Map.ofEntries(
                                Map.entry("message", "Lab Group Deleted Successfully"));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lab Group Not Found");
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Lab Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PostMapping
    public ResponseEntity<Object> createLabGroup(HttpServletRequest request, @RequestBody LabGroupDTO labGroupDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            labGroupDTO.checkDTO();
            if (roomService.isLabGroupExistByName(labGroupDTO.getName()))
                throw new IllegalArgumentException("Name Already Exist");
            if (labGroupDTO.getSpecializations() != null && !labGroupDTO.getSpecializations().isEmpty()) {
                List<String> nonExistentSpecializations = specializationService
                        .checkNonExistentSpecializations(labGroupDTO.getSpecializations());
                if (!nonExistentSpecializations.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Specialization IDs Not Found: " + String.join(", ", nonExistentSpecializations));
                }
            }

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    List<Specialization> specializations = specializationService
                            .findAllSpecializationById(labGroupDTO.getSpecializations());
                    LabGroup createdLabGroup = roomService.createLabGroup(labGroupDTO, user, specializations);
                    data = Map.ofEntries(
                            Map.entry("id", createdLabGroup.getId()),
                            Map.entry("name", createdLabGroup.getName()),
                            Map.entry("specializations", createdLabGroup.getLabSpecializations().stream()
                                    .map(ls -> ls.getSpecializationId().getName())
                                    .collect(Collectors.toList())),
                            Map.entry("createdAt", createdLabGroup.getCreatedAt()),
                            Map.entry("updatedAt", createdLabGroup.getUpdatedAt()));
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Lab Komputer\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "LabGroup Not Found")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PutMapping("/{labGroupId}")
    public ResponseEntity<Object> editLabGroup(HttpServletRequest request, @RequestBody LabGroupDTO labGroupDTO,
            @PathVariable String labGroupId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            labGroupDTO.checkDTO();
            if (roomService.isLabGroupExistByName(labGroupDTO.getName()))
                throw new IllegalArgumentException("Name Already Exist");
            if (labGroupDTO.getSpecializations() != null && !labGroupDTO.getSpecializations().isEmpty()) {
                List<String> nonExistentSpecializations = specializationService
                        .checkNonExistentSpecializations(labGroupDTO.getSpecializations());
                if (!nonExistentSpecializations.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Specialization IDs Not Found: " + String.join(", ", nonExistentSpecializations));
                }
            }

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<LabGroup> editedLabGroupOpt = roomService.findLabGroupById(labGroupId);
                    if (editedLabGroupOpt.isPresent()) {
                        LabGroup editedLabGroup = editedLabGroupOpt.get();
                        List<Specialization> specializations = specializationService
                                .findAllSpecializationById(labGroupDTO.getSpecializations());
                        editedLabGroup = roomService.editLabGroup(editedLabGroup, labGroupDTO, user, specializations);
                        data = Map.ofEntries(
                                Map.entry("id", editedLabGroup.getId()),
                                Map.entry("name", editedLabGroup.getName()),
                                Map.entry("specializations", editedLabGroup.getLabSpecializations().stream()
                                        .map(ls -> ls.getSpecializationId().getName())
                                        .collect(Collectors.toList())),
                                Map.entry("createdAt", editedLabGroup.getCreatedAt()),
                                Map.entry("updatedAt", editedLabGroup.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "LabGroup Not Found");
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
