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
import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.SubMajor;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.dto.CategoryDTO;
import com.jadwal.restfulapi.dto.SubMajorDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/submajor")
public class SubMajorController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"FSD\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping("/{prodiId}")
    public ResponseEntity<Object> getSubMajors(HttpServletRequest request, @PathVariable String prodiId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Category> categoryOpt = categoryService.findProdiById(prodiId);
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        ArrayList<Map<String, Object>> categoryList = new ArrayList<>();
                        for (SubMajor subMajor : categoryService.findAllSubMajorsByCategory(category)) {
                            categoryList.add(Map.of(
                                    "id", subMajor.getId(),
                                    "name", subMajor.getName(),
                                    "createdAt", subMajor.getCreatedAt(),
                                    "updatedAt", subMajor.getUpdatedAt()));
                        }
                        data = categoryList;
                    } else {
                        httpCode = HTTPCode.FORBIDDEN;
                        data = new ErrorMessage(httpCode, "Category Not Prodi");
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"FSD\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "SubMajor Not Found")
    @GetMapping("/{prodiId}/{subMajorId}")
    public ResponseEntity<Object> getSubMajorById(HttpServletRequest request, @PathVariable String prodiId,
            @PathVariable String subMajorId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Category> categoryOpt = categoryService.findProdiById(prodiId);
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        Optional<SubMajor> subMajorOpt = categoryService.findSubMajorByIdAndCategory(subMajorId,
                                category);
                        if (subMajorOpt.isPresent()) {
                            SubMajor subMajor = subMajorOpt.get();
                            data = Map.of(
                                    "id", subMajor.getId(),
                                    "name", subMajor.getName(),
                                    "createdAt", subMajor.getCreatedAt(),
                                    "updatedAt", subMajor.getUpdatedAt());
                        } else {
                            httpCode = HTTPCode.NOT_FOUND;
                            data = new ErrorMessage(httpCode, "SubMajor Not Found");
                        }
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Prodi Not Found");
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

    @SuccessExample(value = "{\"message\":\"SubMajor Deleted Successfully\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "SubMajor Not Found")
    @DeleteMapping("/{prodiId}/{subMajorId}")
    public ResponseEntity<Object> deleteSubMajor(HttpServletRequest request, @PathVariable String prodiId,
            @PathVariable String subMajorId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Category> categoryOpt = categoryService.findProdiById(prodiId);
                    if (categoryOpt.isPresent()) {
                        Category c = categoryOpt.get();
                        Optional<SubMajor> subMajorOpt = categoryService.findSubMajorByIdAndCategory(subMajorId, c);
                        if (subMajorOpt.isPresent()) {
                            SubMajor subMajor = subMajorOpt.get();
                            categoryService.deleteSubMajor(subMajor);
                            data = Map.of(
                                    "message", "SubMajor Deleted Successfully");
                        } else {
                            httpCode = HTTPCode.NOT_FOUND;
                            data = new ErrorMessage(httpCode, "SubMajor Not Found");
                        }
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Prodi Not Found");
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

    @SuccessExample(value = "{\"categoryId\":\"uuid\",\"name\":\"FSD\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PostMapping("/{prodiId}")
    public ResponseEntity<Object> createSubMajor(HttpServletRequest request, @RequestBody SubMajorDTO subMajorDTO,
            @PathVariable String prodiId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    subMajorDTO.checkDTO();
                    Optional<Category> categoryOpt = categoryService.findProdiById(prodiId);
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        if (categoryService.isSubMajorExistByNameAndCategory(subMajorDTO.getName(), category))
                            throw new IllegalArgumentException("Name Already Exist");

                        SubMajor subMajor = categoryService.createSubMajor(subMajorDTO.getName(), category, user);
                        data = Map.of(
                                "id", subMajor.getId(),
                                "name", subMajor.getName(),
                                "createdAt", subMajor.getCreatedAt(),
                                "updatedAt", subMajor.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Prodi Not Found");
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

    @SuccessExample(value = "{\"categoryId\":\"uuid\",\"name\":\"FSD\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PutMapping("/{prodiId}/{subMajorId}")
    public ResponseEntity<Object> editSubMajor(HttpServletRequest request, @RequestBody SubMajorDTO subMajorDTO,
            @PathVariable String prodiId, @PathVariable String subMajorId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    subMajorDTO.checkDTO();
                    Optional<Category> categoryOpt = categoryService.findProdiById(prodiId);
                    if (categoryOpt.isPresent()) {
                        Category category = categoryOpt.get();
                        Optional<SubMajor> subMajorOpt = categoryService.findSubMajorByIdAndCategory(subMajorId,
                                category);
                        if (subMajorOpt.isPresent()) {
                            if (categoryService.isSubMajorExistByNameAndCategoryAndIdNot(subMajorDTO.getName(), category, subMajorId))
                                throw new IllegalArgumentException("Name Already Exist");
                            SubMajor currentSubMajor = subMajorOpt.get();
                            SubMajor subMajor = categoryService.editSubMajor(currentSubMajor, subMajorDTO.getName(), category, user);
                            data = Map.of(
                                    "id", subMajor.getId(),
                                    "name", subMajor.getName(),
                                    "createdAt", subMajor.getCreatedAt(),
                                    "updatedAt", subMajor.getUpdatedAt());
                        } else {
                            httpCode = HTTPCode.NOT_FOUND;
                            data = new ErrorMessage(httpCode, "SubMajor Not Found");
                        }
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Prodi Not Found");
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
