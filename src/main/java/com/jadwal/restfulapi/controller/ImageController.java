package com.jadwal.restfulapi.controller;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.UrlResource;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping(value = "${storage.api-prefix}/images")
public class ImageController {

    private Object data = "";

    @Value("${storage.api-prefix}/images/")
    private String apiEndpoint;

    @NoAuth
    @ErrorExample(code = "404", name = "not-found", message = "Image Not Found")
    @ErrorExample(code = "400", name = "invalid-path", message = "Illegal base64 character 2d")
    @GetMapping("/**")
    public ResponseEntity<Object> getImage(HttpServletRequest request) throws Exception {
        HTTPCode httpCode = HTTPCode.OK;
        try {
            String pathImage = request.getRequestURI()
                    .substring(request.getContextPath().length() + apiEndpoint.length());
            byte[] decodedBytes = Base64.getDecoder().decode(pathImage);
            String fullImagePath = new String(decodedBytes);
            Path path = Paths.get(fullImagePath);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                data = resource;
            } else {
                httpCode = HTTPCode.NOT_FOUND;
                data = new ErrorMessage(httpCode, "Image Not Found");
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
                .contentType(httpCode == HTTPCode.OK ? MediaType.IMAGE_JPEG : MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(data);
    }
}