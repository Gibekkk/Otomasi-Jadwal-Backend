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

import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.CourseService;
import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.dto.CourseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/course")
public class CourseController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CategoryService categoryService;

    private Object data = "";

    @GetMapping("/")
    public ResponseEntity<Object> getCourses(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    List<Course> courses = courseService.findAllCourse();
                    ArrayList<Map<String, Object>> courseList = new ArrayList<>();
                    for (Course course : courses) {
                        courseList.add(Map.of(
                                "id", course.getId(),
                                "name", course.getName(),
                                "sksCount", course.getSksCount(),
                                "category", course.getCategoryId().getName(),
                                "createdAt", course.getCreatedAt(),
                                "updatedAt", course.getUpdatedAt()));
                    }
                    data = courseList;
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
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

    @GetMapping("/{courseId}")
    public ResponseEntity<Object> getCourseById(HttpServletRequest request, @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Course> courseOpt = courseService.findCourseById(courseId);
                    if (courseOpt.isPresent()) {
                        Course c = courseOpt.get();
                        data = Map.of(
                                "id", c.getId(),
                                "name", c.getName(),
                                "sksCount", c.getSksCount(),
                                "category", c.getCategoryId().getName(),
                                "createdAt", c.getCreatedAt(),
                                "updatedAt", c.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
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

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(HttpServletRequest request, @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Course> courseOpt = courseService.findCourseById(courseId);
                    if (courseOpt.isPresent()) {
                        Course c = courseOpt.get();
                        courseService.deleteCourse(c);
                        data = Map.of(
                                "message", "Course Deleted Successfully");
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
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

    @PostMapping("/")
    public ResponseEntity<Object> createCourse(HttpServletRequest request, @RequestBody CourseDTO courseDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            courseDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Category category = categoryService.findCategoryById(courseDTO.getCategoryId()).get();
                    Course createdCourse = courseService.createCourse(courseDTO, category, user);
                    data = Map.of(
                            "courseId", createdCourse.getId(),
                            "name", createdCourse.getName(),
                            "sksCount", createdCourse.getSksCount(),
                            "category", createdCourse.getCategoryId().getName(),
                            "createdAt", createdCourse.getCreatedAt(),
                            "updatedAt", createdCourse.getUpdatedAt());
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
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

    @PutMapping("/{courseId}")
    public ResponseEntity<Object> editCourse(HttpServletRequest request, @RequestBody CourseDTO courseDTO,
            @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            courseDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                    Optional<Course> editedCourseOpt = courseService.findCourseById(courseId);
                    if (editedCourseOpt.isPresent()) {
                        Course editedCourse = editedCourseOpt.get();
                        Category category = categoryService.findCategoryById(courseDTO.getCategoryId()).get();
                        editedCourse = courseService.editCourse(editedCourse, courseDTO, category, user);
                        data = Map.of(
                                "courseId", editedCourse.getId(),
                                "name", editedCourse.getName(),
                                "sksCount", editedCourse.getSksCount(),
                                "category", editedCourse.getCategoryId().getName(),
                                "createdAt", editedCourse.getCreatedAt(),
                                "updatedAt", editedCourse.getUpdatedAt());
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
                    }
                } else {
                    httpCode = HTTPCode.FORBIDDEN;
                    data = new ErrorMessage(httpCode, "Access Denied");
                }
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
