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
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.dto.CategoryDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/category")
public class CategoryController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryService categoryService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Wajib\",\"isProdi\":false,"
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping
    public ResponseEntity<Object> getCategory(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    List<Category> categories = categoryService.findAllCategory();
                    if (authService.isProdiAdmin(user))
                        categories.stream().filter(category -> category.equals(user.getProdiId())).toList();
                    if (authService.isNtHumAdmin(user))
                        categories.stream().filter(category -> category.getName().equals("Umum")
                                || category.getName().equals("Entrepreneurship")).toList();
                    ArrayList<Map<String, Object>> categoryList = new ArrayList<>();
                    for (Category category : categories) {
                        categoryList.add(Map.of(
                                "id", category.getId(),
                                "name", category.getName(),
                                "isProdi", category.getIsProdi(),
                                "createdAt", category.getCreatedAt(),
                                "updatedAt", category.getUpdatedAt()));
                    }
                    data = categoryList;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Wajib\",\"isProdi\":false,"
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Category Not Found")
    @GetMapping("/{categoryId}")
    public ResponseEntity<Object> getCategoryById(HttpServletRequest request, @PathVariable String categoryId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Category> categoryOpt = categoryService.findCategoryById(categoryId);
                    if (categoryOpt.isPresent()) {
                        Category c = categoryOpt.get();

                        Boolean accessGranted = true;
                        if (authService.isProdiAdmin(user))
                            accessGranted = c.equals(user.getProdiId());
                        else if (authService.isNtHumAdmin(user))
                            accessGranted = c.getName().equals("Umum") || c.getName().equals("Entrepreneurship");

                        if (accessGranted) {
                            data = Map.of(
                                    "id", c.getId(),
                                    "name", c.getName(),
                                    "isProdi", c.getIsProdi(),
                                    "createdAt", c.getCreatedAt(),
                                    "updatedAt", c.getUpdatedAt());
                        } else {
                            httpCode = HTTPCode.FORBIDDEN;
                            data = new ErrorMessage(httpCode, "Access Denied");
                        }
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Category Not Found");
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

    @SuccessExample(value = "{\"message\":\"Category Deleted Successfully\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Category Not Found")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Object> deleteCategory(HttpServletRequest request, @PathVariable String categoryId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Category> categoryOpt = categoryService.findCategoryById(categoryId);
                    if (categoryOpt.isPresent()) {
                        Category c = categoryOpt.get();
                        categoryService.deleteCategory(c);
                        data = Map.of(
                                "message", "Category Deleted Successfully");
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Category Not Found");
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

    @SuccessExample(value = "{\"categoryId\":\"uuid\",\"name\":\"Wajib\",\"isProdi\":false,"
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PostMapping
    public ResponseEntity<Object> createCategory(HttpServletRequest request, @RequestBody CategoryDTO categoryDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            categoryDTO.checkDTO();
            if (categoryService.isCategoryExistByName(categoryDTO.getName()))
                throw new IllegalArgumentException("Name Already Exist");

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Category category = categoryService.createCategory(categoryDTO, user);
                    data = Map.of(
                            "categoryId", category.getId(),
                            "name", category.getName(),
                            "isProdi", category.getIsProdi(),
                            "createdAt", category.getCreatedAt(),
                            "updatedAt", category.getUpdatedAt());
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

    @SuccessExample(value = "{\"categoryId\":\"uuid\",\"name\":\"Wajib\",\"isProdi\":false,"
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "Category Not Found")
    @ErrorExample(code = "400", name = "invalid-body", message = "Name Cannot Be NULL")
    @PutMapping("/{categoryId}")
    public ResponseEntity<Object> editCategory(HttpServletRequest request, @RequestBody CategoryDTO categoryDTO,
            @PathVariable String categoryId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            categoryDTO.checkDTO();

            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Category> editedCategoryOpt = categoryService.findCategoryById(categoryId);
                    if (editedCategoryOpt.isPresent()) {
                        Category editedCategory = editedCategoryOpt.get();
                        if (categoryService.isCategoryExistByNameAndIdIsNot(categoryDTO.getName(),
                                editedCategory.getId()))
                            throw new IllegalArgumentException("Name Already Exist");

                        editedCategory = categoryService.editCategory(editedCategory, categoryDTO, user);
                        data = Map.of(
                                "categoryId", editedCategory.getId(),
                                "name", editedCategory.getName(),
                                "isProdi", editedCategory.getIsProdi(),
                                "createdAt", editedCategory.getCreatedAt(),
                                "updatedAt", editedCategory.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Category Not Found");
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
