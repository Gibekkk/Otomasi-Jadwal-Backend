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
import com.jadwal.restfulapi.service.UserService;
import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.service.UserGroupService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.UserGroup;
import com.jadwal.restfulapi.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/user")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserGroupService userGroupService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"BAA Admin\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping("/groups")
    public ResponseEntity<Object> getGroups(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user)) {
                    ArrayList<Map<String, Object>> groupData = new ArrayList<Map<String, Object>>();
                    List<UserGroup> groups = userGroupService.findAllAndNotSuperAdmin();
                    for (UserGroup group : groups) {
                        groupData.add(Map.of(
                                "id", group.getId(),
                                "name", group.getName()));
                    }
                    data = groupData;
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

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Budi Santoso\",\"username\":\"budi\","
            + "\"group\":\"Baa Admin\",\"prodi\":\"Teknik Informatika\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @GetMapping
    public ResponseEntity<Object> getUsers(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user)) {
                    ArrayList<Map<String, Object>> userData = new ArrayList<Map<String, Object>>();
                    List<User> users = userService.findAllUser();
                    for (User u : users) {
                        userData.add(Map.of(
                                "id", u.getId(),
                                "name", u.getName(),
                                "username", u.getUsername(),
                                "group", u.getGroupId().getName(),
                                "prodi", Optional.ofNullable(u.getProdiId()).map(prodi -> prodi.getName()).orElse("-"),
                                "createdAt", u.getCreatedAt(),
                                "updatedAt", u.getUpdatedAt()));
                    }
                    data = userData;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Budi Santoso\",\"username\":\"budi\","
            + "\"group\":\"Baa Admin\",\"prodi\":\"Teknik Informatika\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "User Not Found")
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(HttpServletRequest request, @PathVariable String userId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user)) {
                    Optional<User> userOpt = userService.findUserById(userId);
                    if (userOpt.isPresent()) {
                        User u = userOpt.get();
                        data = Map.of(
                                "id", u.getId(),
                                "name", u.getName(),
                                "username", u.getUsername(),
                                "group", u.getGroupId().getName(),
                                "prodi", Optional.ofNullable(u.getProdiId()).map(prodi -> prodi.getName()).orElse("-"),
                                "createdAt", u.getCreatedAt(),
                                "updatedAt", u.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "User Not Found");
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

    @SuccessExample(value = "{\"message\":\"User Deleted Successfully\"}")
    @ErrorExample(code = "401", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "User Not Found")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(HttpServletRequest request, @PathVariable String userId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user)) {
                    Optional<User> userOpt = userService.findUserById(userId);
                    if (userOpt.isPresent()) {
                        User u = userOpt.get();
                        userService.deleteUser(u);
                        data = Map.of(
                                "message", "User Deleted Successfully");
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "User Not Found");
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

    @SuccessExample(value = "{\"userId\":\"uuid\",\"name\":\"Budi Santoso\",\"username\":\"budi\","
            + "\"group\":\"Baa Admin\",\"prodi\":\"Teknik Informatika\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @ErrorExample(code = "400", name = "invalid-body", message = "Username Cannot Be NULL")
    @ErrorExample(code = "403", name = "group-invalid", message = "Group ID Invalid")
    @ErrorExample(code = "403", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @PostMapping
    public ResponseEntity<Object> createUser(HttpServletRequest request, @RequestBody UserDTO userDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            userDTO.checkDTO();
            if (userDTO.getProdiId() != null && !categoryService.isProdiExistById(userDTO.getProdiId()))
                throw new IllegalArgumentException("Prodi ID Not Found");
            if (!userGroupService.isProdiExistById(userDTO.getGroupId()))
                throw new IllegalArgumentException("Group ID Not Found");

            Optional<UserGroup> userGroupOpt = userGroupService.findUserGroupByIdAndNotSuperAdmin(userDTO.getGroupId());
            if (userGroupOpt.isPresent()) {
                UserGroup userGroup = userGroupOpt.get();
                Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
                if (sessionOpt.isPresent()) {
                    Session session = sessionOpt.get();
                    User user = session.getUserId();
                    if (authService.isSuperAdmin(user)) {
                        User createdUser = new User();
                        if (userDTO.getProdiId() == null) {
                            createdUser = userService.createUser(userDTO, userGroup);
                        } else {
                            Category prodi = categoryService.findProdiById(userDTO.getProdiId()).get();
                            createdUser = userService.createUser(userDTO, userGroup, prodi);
                        }
                        data = Map.of(
                                "userId", createdUser.getId(),
                                "name", createdUser.getName(),
                                "username", createdUser.getUsername(),
                                "group", createdUser.getGroupId().getName(),
                                "prodi",
                                Optional.ofNullable(createdUser.getProdiId()).map(prodi -> prodi.getName()).orElse("-"),
                                "createdAt", createdUser.getCreatedAt(),
                                "updatedAt", createdUser.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.FORBIDDEN;
                        data = new ErrorMessage(httpCode, "Access Denied");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Authentication Failed");
                }
            } else {
                httpCode = HTTPCode.FORBIDDEN;
                data = new ErrorMessage(httpCode, "Group ID Invalid");
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

    @SuccessExample(value = "{\"userId\":\"uuid\",\"name\":\"Budi Santoso\",\"username\":\"budi\","
            + "\"group\":\"Baa Admin\",\"prodi\":\"Teknik Informatika\","
            + "\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @ErrorExample(code = "400", name = "invalid-body", message = "Username Cannot Be NULL")
    @ErrorExample(code = "403", name = "group-invalid", message = "Group ID Invalid")
    @ErrorExample(code = "403", name = "session-invalid", message = "Authentication Failed")
    @ErrorExample(code = "403", name = "access-denied", message = "Access Denied")
    @ErrorExample(code = "404", name = "not-found", message = "User Not Found")
    @PutMapping("/{userId}")
    public ResponseEntity<Object> editUser(HttpServletRequest request, @RequestBody UserDTO userDTO,
            @PathVariable String userId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            userDTO.checkDTO();
            if (userDTO.getProdiId() != null && !categoryService.isProdiExistById(userDTO.getProdiId()))
                throw new IllegalArgumentException("Prodi ID Not Found");
            if (!userGroupService.isProdiExistById(userDTO.getGroupId()))
                throw new IllegalArgumentException("Group ID Not Found");

            Optional<UserGroup> userGroupOpt = userGroupService.findUserGroupByIdAndNotSuperAdmin(userDTO.getGroupId());
            if (userGroupOpt.isPresent()) {
                UserGroup userGroup = userGroupOpt.get();
                Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
                if (sessionOpt.isPresent()) {
                    Session session = sessionOpt.get();
                    User user = session.getUserId();
                    if (authService.isSuperAdmin(user)) {
                        Optional<User> editedUserOpt = userService.findUserById(userId);
                        if (editedUserOpt.isPresent()) {
                            User editedUser = editedUserOpt.get();
                            if (userDTO.getProdiId() == null) {
                                editedUser = userService.editUser(editedUser, userDTO, userGroup);
                            } else {
                                Category prodi = categoryService.findProdiById(userDTO.getProdiId()).get();
                                editedUser = userService.editUser(editedUser, userDTO, userGroup, prodi);
                            }
                            data = Map.of(
                                    "userId", editedUser.getId(),
                                    "name", editedUser.getName(),
                                    "username", editedUser.getUsername(),
                                    "group", editedUser.getGroupId().getName(),
                                    "prodi",
                                    Optional.ofNullable(editedUser.getProdiId()).map(prodi -> prodi.getName())
                                            .orElse("-"),
                                    "createdAt", editedUser.getCreatedAt(),
                                    "updatedAt", editedUser.getUpdatedAt());
                        } else {
                            httpCode = HTTPCode.NOT_FOUND;
                            data = new ErrorMessage(httpCode, "User Not Found");
                        }
                    } else {
                        httpCode = HTTPCode.FORBIDDEN;
                        data = new ErrorMessage(httpCode, "Access Denied");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Authentication Failed");
                }
            } else {
                httpCode = HTTPCode.FORBIDDEN;
                data = new ErrorMessage(httpCode, "Group ID Invalid");
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
