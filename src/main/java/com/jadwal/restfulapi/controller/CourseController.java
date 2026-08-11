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
import org.springframework.web.bind.annotation.PatchMapping;

import com.jadwal.restfulapi.annotation.NotFoundExample;
import com.jadwal.restfulapi.annotation.SuccessExample;
import com.jadwal.restfulapi.service.AuthService;
import com.jadwal.restfulapi.service.CourseService;
import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.service.SpecializationService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Course;
import com.jadwal.restfulapi.dto.CourseDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @Autowired
    private SpecializationService specializationService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @GetMapping("/")
    public ResponseEntity<Object> getCourses(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    List<Course> courses = new ArrayList<Course>();
                    if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                        courses = courseService.findAllCourse();
                    } else if (authService.isProdiAdmin(user)) {
                        courses = courseService.findCourseByCategoryAndInterdicipline(user.getProdiId());
                    } else if (authService.isNtHumAdmin(user)) {
                        courses = courseService.findAllCourse()
                                .stream()
                                .filter(course -> course.getIsInterdicipline() == true
                                        || course.getCategoryId().getName().equals("Umum")
                                        || course.getCategoryId().getName().equals("Entrepreneurship"))
                                .collect(Collectors.toList());
                    }
                    ArrayList<Map<String, Object>> courseList = new ArrayList<>();
                    for (Course course : courses) {
                        courseList.add(Map.ofEntries(
                                Map.entry("id", course.getId()),
                                Map.entry("name", course.getName()),
                                Map.entry("sksCount", course.getSksCount()),
                                Map.entry("lecturerCount", course.getLecturerCount()),
                                Map.entry("capacity", course.getCapacity()),
                                Map.entry("isInterdicipline", course.getIsInterdicipline()),
                                Map.entry("isOdd", course.getIsOdd()),
                                Map.entry("isActive", course.getIsActive()),
                                Map.entry("isLab", course.getIsLab()),
                                Map.entry("category", course.getCategoryId().getName()),
                                Map.entry("createdAt", course.getCreatedAt()),
                                Map.entry("updatedAt", course.getUpdatedAt()),
                                Map.entry("specializations", course.getCourseSpecializations().stream()
                                        .map(cs -> cs.getSpecializationId().getName())
                                        .collect(Collectors.toList()))));
                    }
                    data = courseList;
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

    @SuccessExample(value = "{\"id\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @NotFoundExample("Course Not Found")
    @GetMapping("/{courseId}")
    public ResponseEntity<Object> getCourseById(HttpServletRequest request, @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Course> courseOpt = Optional.empty();
                    if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                        courseOpt = courseService.findCourseById(courseId);
                    } else if (authService.isProdiAdmin(user)) {
                        courseOpt = courseService.findCourseByIdAndCategoryAndInterdicipline(courseId,
                                user.getProdiId());
                    } else if (authService.isNtHumAdmin(user)) {
                        courseOpt = courseService.findCourseById(courseId)
                                .filter(course -> course.getIsInterdicipline()
                                        || course.getCategoryId().getName().equals("Umum")
                                        || course.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (courseOpt.isPresent()) {
                        Course c = courseOpt.get();
                        data = Map.ofEntries(
                                Map.entry("id", c.getId()),
                                Map.entry("name", c.getName()),
                                Map.entry("sksCount", c.getSksCount()),
                                Map.entry("lecturerCount", c.getLecturerCount()),
                                Map.entry("capacity", c.getCapacity()),
                                Map.entry("isInterdicipline", c.getIsInterdicipline()),
                                Map.entry("isOdd", c.getIsOdd()),
                                Map.entry("isActive", c.getIsActive()),
                                Map.entry("isLab", c.getIsLab()),
                                Map.entry("category", c.getCategoryId().getName()),
                                Map.entry("createdAt", c.getCreatedAt()),
                                Map.entry("updatedAt", c.getUpdatedAt()),
                                Map.entry("specializations", c.getCourseSpecializations().stream()
                                        .map(cs -> cs.getSpecializationId().getName())
                                        .collect(Collectors.toList())));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
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

    @SuccessExample(value = "{\"message\":\"Course Deleted Successfully\"}")
    @NotFoundExample("Course Not Found")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(HttpServletRequest request, @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Course> courseOpt = Optional.empty();
                    if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                        courseOpt = courseService.findCourseById(courseId);
                    } else if (authService.isProdiAdmin(user)) {
                        courseOpt = courseService.findCourseByIdAndCategoryAndInterdicipline(courseId,
                                user.getProdiId());
                    } else if (authService.isNtHumAdmin(user)) {
                        courseOpt = courseService.findCourseById(courseId)
                                .filter(course -> course.getIsInterdicipline()
                                        || course.getCategoryId().getName().equals("Umum")
                                        || course.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (courseOpt.isPresent()) {
                        Course c = courseOpt.get();
                        courseService.deleteCourse(c);
                        data = Map.ofEntries(
                                Map.entry("message", "Course Deleted Successfully"));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
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

    @SuccessExample(value = "{\"courseId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
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
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Category category = categoryService.findCategoryById(courseDTO.getCategoryId()).get();
                    if ((authService.isProdiAdmin(user) && !user.getProdiId().equals(category)) ||
                            (authService.isNtHumAdmin(user) && !(category.getName().equals("Umum")
                                    || category.getName().equals("Entrepreneurship")))) {
                        httpCode = HTTPCode.FORBIDDEN;
                        data = new ErrorMessage(httpCode, "Category Not Permitted");
                        return ResponseEntity
                                .status(httpCode.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(data);
                    }
                    List<Specialization> specializations = specializationService
                            .findAllSpecializationById(courseDTO.getSpecializations());
                    Course createdCourse = courseService.createCourse(courseDTO, category, user, specializations);
                    data = Map.ofEntries(
                            Map.entry("courseId", createdCourse.getId()),
                            Map.entry("name", createdCourse.getName()),
                            Map.entry("sksCount", createdCourse.getSksCount()),
                            Map.entry("lecturerCount", createdCourse.getLecturerCount()),
                            Map.entry("capacity", createdCourse.getCapacity()),
                            Map.entry("isInterdicipline", createdCourse.getIsInterdicipline()),
                            Map.entry("isOdd", createdCourse.getIsOdd()),
                            Map.entry("isActive", createdCourse.getIsActive()),
                            Map.entry("isLab", createdCourse.getIsLab()),
                            Map.entry("category", createdCourse.getCategoryId().getName()),
                            Map.entry("createdAt", createdCourse.getCreatedAt()),
                            Map.entry("updatedAt", createdCourse.getUpdatedAt()),
                            Map.entry("specializations", createdCourse.getCourseSpecializations().stream()
                                    .map(cs -> cs.getSpecializationId().getName())
                                    .collect(Collectors.toList())));
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

    @SuccessExample(value = "{\"courseId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @NotFoundExample("Course Not Found")
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
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Course> editedCourseOpt = Optional.empty();
                    if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                        editedCourseOpt = courseService.findCourseById(courseId);
                    } else if (authService.isProdiAdmin(user)) {
                        editedCourseOpt = courseService.findCourseByIdAndCategoryAndInterdicipline(courseId,
                                user.getProdiId());
                    } else if (authService.isNtHumAdmin(user)) {
                        editedCourseOpt = courseService.findCourseById(courseId)
                                .filter(course -> course.getIsInterdicipline()
                                        || course.getCategoryId().getName().equals("Umum")
                                        || course.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (editedCourseOpt.isPresent()) {
                        Course editedCourse = editedCourseOpt.get();
                        Category category = categoryService.findCategoryById(courseDTO.getCategoryId()).get();
                        List<Specialization> specializations = specializationService
                                .findAllSpecializationById(courseDTO.getSpecializations());
                        editedCourse = courseService.editCourse(editedCourse, courseDTO, category, user,
                                specializations);
                        data = Map.ofEntries(
                                Map.entry("courseId", editedCourse.getId()),
                                Map.entry("name", editedCourse.getName()),
                                Map.entry("sksCount", editedCourse.getSksCount()),
                                Map.entry("lecturerCount", editedCourse.getLecturerCount()),
                                Map.entry("capacity", editedCourse.getCapacity()),
                                Map.entry("isInterdicipline", editedCourse.getIsInterdicipline()),
                                Map.entry("isOdd", editedCourse.getIsOdd()),
                                Map.entry("isActive", editedCourse.getIsActive()),
                                Map.entry("isLab", editedCourse.getIsLab()),
                                Map.entry("category", editedCourse.getCategoryId().getName()),
                                Map.entry("createdAt", editedCourse.getCreatedAt()),
                                Map.entry("updatedAt", editedCourse.getUpdatedAt()),
                                Map.entry("specializations", editedCourse.getCourseSpecializations().stream()
                                        .map(cs -> cs.getSpecializationId().getName())
                                        .collect(Collectors.toList())));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
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

    @SuccessExample(value = "{\"courseId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":false,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @NotFoundExample("Course Not Found")
    @PatchMapping("/toggle/{courseId}")
    public ResponseEntity<Object> toggleCourseActive(HttpServletRequest request, @PathVariable String courseId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Course> editedCourseOpt = Optional.empty();
                    if (authService.isSuperAdmin(user) || authService.isBaaAdmin(user)) {
                        editedCourseOpt = courseService.findCourseById(courseId);
                    } else if (authService.isProdiAdmin(user)) {
                        editedCourseOpt = courseService.findCourseByIdAndCategoryAndInterdicipline(courseId,
                                user.getProdiId());
                    } else if (authService.isNtHumAdmin(user)) {
                        editedCourseOpt = courseService.findCourseById(courseId)
                                .filter(course -> course.getIsInterdicipline()
                                        || course.getCategoryId().getName().equals("Umum")
                                        || course.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (editedCourseOpt.isPresent()) {
                        Course editedCourse = editedCourseOpt.get();
                        Boolean isActive = courseService.toggleCourseActive(editedCourse);
                        data = Map.ofEntries(
                                Map.entry("courseId", editedCourse.getId()),
                                Map.entry("name", editedCourse.getName()),
                                Map.entry("sksCount", editedCourse.getSksCount()),
                                Map.entry("lecturerCount", editedCourse.getLecturerCount()),
                                Map.entry("capacity", editedCourse.getCapacity()),
                                Map.entry("isInterdicipline", editedCourse.getIsInterdicipline()),
                                Map.entry("isOdd", editedCourse.getIsOdd()),
                                Map.entry("isActive", isActive),
                                Map.entry("isLab", editedCourse.getIsLab()),
                                Map.entry("category", editedCourse.getCategoryId().getName()),
                                Map.entry("createdAt", editedCourse.getCreatedAt()),
                                Map.entry("updatedAt", editedCourse.getUpdatedAt()),
                                Map.entry("specializations", editedCourse.getCourseSpecializations().stream()
                                        .map(cs -> cs.getSpecializationId().getName())
                                        .collect(Collectors.toList())));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Course Not Found");
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
