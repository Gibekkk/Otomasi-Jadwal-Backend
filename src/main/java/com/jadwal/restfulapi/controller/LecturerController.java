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
import com.jadwal.restfulapi.service.LecturerService;
import com.jadwal.restfulapi.service.CategoryService;
import com.jadwal.restfulapi.service.SpecializationService;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import com.jadwal.restfulapi.model.Session;
import com.jadwal.restfulapi.model.Specialization;
import com.jadwal.restfulapi.model.Category;
import com.jadwal.restfulapi.model.User;
import com.jadwal.restfulapi.model.Lecturer;
import com.jadwal.restfulapi.dto.LecturerDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/lecturer")
public class LecturerController {

    @Autowired
    private AuthService authService;

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpecializationService specializationService;

    private Object data = "";

    @SuccessExample(value = "[{\"id\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}]")
    @GetMapping("/")
    public ResponseEntity<Object> getLecturers(HttpServletRequest request) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    List<Lecturer> lecturers = new ArrayList<Lecturer>();
                    if (authService.isSuperAdmin(user) || authService.isNtHumAdmin(user)) {
                        lecturers = lecturerService.findAllLecturer();
                    } else if (authService.isProdiAdmin(user)) {
                        Category category = user.getProdiId();
                        lecturers = lecturerService.findLecturerByCategoryAndInterdicipline(category);
                    } else if (authService.isNtHumAdmin(user)) {
                        lecturers = lecturerService.findAllLecturer()
                                .stream()
                                .filter(lecturer -> lecturer.getIsInterdicipline()
                                        || lecturer.getCategoryId().getName().equals("Umum")
                                        || lecturer.getCategoryId().getName().equals("Entrepreneurship"))
                                .toList();
                    }
                    ArrayList<Map<String, Object>> lecturerList = new ArrayList<>();
                    for (Lecturer lecturer : lecturers) {
                        lecturerList.add(Map.ofEntries(
                                Map.entry("id", lecturer.getId()),
                                Map.entry("name", lecturer.getName()),
                                Map.entry("isDlb", lecturer.getIsDlb()),
                                Map.entry("isMale", lecturer.getIsMale()),
                                Map.entry("isActive", lecturer.getIsActive()),
                                Map.entry("isInterdicipline", lecturer.getIsInterdicipline()),
                                Map.entry("religion", lecturer.getReligion().toString()),
                                Map.entry("category", lecturer.getCategoryId().getName()),
                                Map.entry("createdAt", lecturer.getCreatedAt()),
                                Map.entry("updatedAt", lecturer.getUpdatedAt())));
                    }
                    data = lecturerList;
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
    @NotFoundExample("Lecturer Not Found")
    @GetMapping("/{lecturerId}")
    public ResponseEntity<Object> getLecturerById(HttpServletRequest request, @PathVariable String lecturerId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)) {
                    Optional<Lecturer> lecturerOpt = Optional.empty();
                    if (authService.isSuperAdmin(user)) {
                        lecturerOpt = lecturerService.findLecturerById(lecturerId);
                    } else if (authService.isProdiAdmin(user)) {
                        Category category = user.getProdiId();
                        lecturerOpt = lecturerService.findLecturerByIdAndCategoryAndInterdicipline(lecturerId,
                                category);
                    } else if (authService.isNtHumAdmin(user)) {
                        lecturerOpt = lecturerService.findLecturerById(lecturerId)
                                .filter(lecturer -> lecturer.getIsInterdicipline()
                                        || lecturer.getCategoryId().getName().equals("Umum")
                                        || lecturer.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (lecturerOpt.isPresent()) {
                        Lecturer lecturer = lecturerOpt.get();
                        data = Map.ofEntries(
                                Map.entry("id", lecturer.getId()),
                                Map.entry("name", lecturer.getName()),
                                Map.entry("isDlb", lecturer.getIsDlb()),
                                Map.entry("isMale", lecturer.getIsMale()),
                                Map.entry("isActive", lecturer.getIsActive()),
                                Map.entry("isInterdicipline", lecturer.getIsInterdicipline()),
                                Map.entry("religion", lecturer.getReligion().toString()),
                                Map.entry("category", lecturer.getCategoryId().getName()),
                                Map.entry("createdAt", lecturer.getCreatedAt()),
                                Map.entry("updatedAt", lecturer.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lecturer Not Found");
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

    @SuccessExample(value = "{\"message\":\"Lecturer Deleted Successfully\"}")
    @NotFoundExample("Lecturer Not Found")
    @DeleteMapping("/{lecturerId}")
    public ResponseEntity<Object> deleteLecturer(HttpServletRequest request, @PathVariable String lecturerId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Lecturer> lecturerOpt = Optional.empty();
                    if (authService.isSuperAdmin(user)) {
                        lecturerOpt = lecturerService.findLecturerById(lecturerId);
                    } else if (authService.isProdiAdmin(user)) {
                        Category category = user.getProdiId();
                        lecturerOpt = lecturerService.findLecturerByIdAndCategoryAndInterdicipline(lecturerId,
                                category);
                    } else if (authService.isNtHumAdmin(user)) {
                        lecturerOpt = lecturerService.findLecturerById(lecturerId)
                                .filter(lecturer -> lecturer.getIsInterdicipline()
                                        || lecturer.getCategoryId().getName().equals("Umum")
                                        || lecturer.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (lecturerOpt.isPresent()) {
                        Lecturer lecturer = lecturerOpt.get();
                        lecturerService.deleteLecturer(lecturer);
                        data = Map.ofEntries(
                                Map.entry("message", "Lecturer Deleted Successfully"));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lecturer Not Found");
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

    @SuccessExample(value = "{\"lecturerId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-01T00:00:00\"}")
    @PostMapping("/")
    public ResponseEntity<Object> createLecturer(HttpServletRequest request, @RequestBody LecturerDTO lecturerDTO) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            lecturerDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Category category = categoryService.findCategoryById(lecturerDTO.getCategoryId()).get();
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
                            .findAllSpecializationById(lecturerDTO.getSpecializations());
                    Lecturer createdLecturer = lecturerService.createLecturer(lecturerDTO, category, user,
                            specializations);
                    data = Map.ofEntries(
                            Map.entry("id", createdLecturer.getId()),
                            Map.entry("name", createdLecturer.getName()),
                            Map.entry("isDlb", createdLecturer.getIsDlb()),
                            Map.entry("isMale", createdLecturer.getIsMale()),
                            Map.entry("isActive", createdLecturer.getIsActive()),
                            Map.entry("isInterdicipline", createdLecturer.getIsInterdicipline()),
                            Map.entry("religion", createdLecturer.getReligion().toString()),
                            Map.entry("category", createdLecturer.getCategoryId().getName()),
                            Map.entry("createdAt", createdLecturer.getCreatedAt()),
                            Map.entry("updatedAt", createdLecturer.getUpdatedAt()));
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

    @SuccessExample(value = "{\"lecturerId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":true,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @NotFoundExample("Lecturer Not Found")
    @PutMapping("/{lecturerId}")
    public ResponseEntity<Object> editLecturer(HttpServletRequest request, @RequestBody LecturerDTO lecturerDTO,
            @PathVariable String lecturerId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            lecturerDTO.checkDTO();
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Lecturer> editedLecturerOpt = Optional.empty();
                    if (authService.isSuperAdmin(user)) {
                        editedLecturerOpt = lecturerService.findLecturerById(lecturerId);
                    } else if (authService.isProdiAdmin(user)) {
                        Category category = user.getProdiId();
                        editedLecturerOpt = lecturerService.findLecturerByIdAndCategoryAndInterdicipline(lecturerId,
                                category);
                    } else if (authService.isNtHumAdmin(user)) {
                        editedLecturerOpt = lecturerService.findLecturerById(lecturerId)
                                .filter(lecturer -> lecturer.getIsInterdicipline()
                                        || lecturer.getCategoryId().getName().equals("Umum")
                                        || lecturer.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (editedLecturerOpt.isPresent()) {
                        Lecturer editedLecturer = editedLecturerOpt.get();
                        Category category = categoryService.findCategoryById(lecturerDTO.getCategoryId()).get();
                        List<Specialization> specializations = specializationService
                                .findAllSpecializationById(lecturerDTO.getSpecializations());
                        editedLecturer = lecturerService.editLecturer(editedLecturer, lecturerDTO, category, user,
                                specializations);
                        data = Map.ofEntries(
                                Map.entry("id", editedLecturer.getId()),
                                Map.entry("name", editedLecturer.getName()),
                                Map.entry("isDlb", editedLecturer.getIsDlb()),
                                Map.entry("isMale", editedLecturer.getIsMale()),
                                Map.entry("isActive", editedLecturer.getIsActive()),
                                Map.entry("isInterdicipline", editedLecturer.getIsInterdicipline()),
                                Map.entry("religion", editedLecturer.getReligion().toString()),
                                Map.entry("category", editedLecturer.getCategoryId().getName()),
                                Map.entry("createdAt", editedLecturer.getCreatedAt()),
                                Map.entry("updatedAt", editedLecturer.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lecturer Not Found");
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

    @SuccessExample(value = "{\"lecturerId\":\"uuid\",\"name\":\"Algoritma\",\"sksCount\":3,\"lecturerCount\":1,"
            + "\"capacity\":40,\"isInterdicipline\":false,\"isOdd\":true,\"isActive\":false,\"isLab\":false,"
            + "\"category\":\"Wajib\",\"createdAt\":\"2026-01-01T00:00:00\",\"updatedAt\":\"2026-01-02T00:00:00\"}")
    @NotFoundExample("Lecturer Not Found")
    @PatchMapping("/toggle/{lecturerId}")
    public ResponseEntity<Object> toggleLecturerActive(HttpServletRequest request, @PathVariable String lecturerId) {
        String sessionToken = request.getHeader("Token");
        HTTPCode httpCode = HTTPCode.OK;
        try {
            Optional<Session> sessionOpt = authService.findSessionBySessionToken(sessionToken);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                User user = session.getUserId();
                if (authService.isSuperAdmin(user) || authService.isProdiAdmin(user)
                        || authService.isNtHumAdmin(user)) {
                    Optional<Lecturer> editedLecturerOpt = Optional.empty();
                    if (authService.isSuperAdmin(user)) {
                        editedLecturerOpt = lecturerService.findLecturerById(lecturerId);
                    } else if (authService.isProdiAdmin(user)) {
                        Category category = user.getProdiId();
                        editedLecturerOpt = lecturerService.findLecturerByIdAndCategoryAndInterdicipline(lecturerId,
                                category);
                    } else if (authService.isNtHumAdmin(user)) {
                        editedLecturerOpt = lecturerService.findLecturerById(lecturerId)
                                .filter(lecturer -> lecturer.getIsInterdicipline()
                                        || lecturer.getCategoryId().getName().equals("Umum")
                                        || lecturer.getCategoryId().getName().equals("Entrepreneurship"));
                    }
                    if (editedLecturerOpt.isPresent()) {
                        Lecturer editedLecturer = editedLecturerOpt.get();
                        Boolean isActive = lecturerService.toggleLecturerActive(editedLecturer);
                        data = Map.ofEntries(
                                Map.entry("id", editedLecturer.getId()),
                                Map.entry("name", editedLecturer.getName()),
                                Map.entry("isDlb", editedLecturer.getIsDlb()),
                                Map.entry("isMale", editedLecturer.getIsMale()),
                                Map.entry("isActive", isActive),
                                Map.entry("isInterdicipline", editedLecturer.getIsInterdicipline()),
                                Map.entry("religion", editedLecturer.getReligion().toString()),
                                Map.entry("category", editedLecturer.getCategoryId().getName()),
                                Map.entry("createdAt", editedLecturer.getCreatedAt()),
                                Map.entry("updatedAt", editedLecturer.getUpdatedAt()));
                    } else {
                        httpCode = HTTPCode.NOT_FOUND;
                        data = new ErrorMessage(httpCode, "Lecturer Not Found");
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
