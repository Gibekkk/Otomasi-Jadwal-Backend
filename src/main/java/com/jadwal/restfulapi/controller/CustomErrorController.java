package com.jadwal.restfulapi.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jadwal.restfulapi.annotation.NoAuth;
import com.jadwal.restfulapi.annotation.ErrorExample;
import com.jadwal.restfulapi.util.ErrorMessage;
import com.jadwal.restfulapi.util.HTTPCode;
import org.springframework.http.MediaType;

@NoAuth
@RestController
public class CustomErrorController implements ErrorController {

    private Object data = "";

    @ErrorExample(code = "404", name = "route-not-found", message = "Route Not Found")
    @ErrorExample(code = "405", name = "method-not-allowed", message = "Method For This Route Is Incorrect")
    @ErrorExample(code = "500", name = "unexpected", message = "Unknown Error Occurred Outside Main API")
    @RequestMapping("/error")
    public ResponseEntity<Object> handleError(HttpServletRequest request) {
        int statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        HTTPCode httpCode = HTTPCode.fromCode(statusCode);
        if (httpCode.equals(HTTPCode.NOT_FOUND)) {
            data = new ErrorMessage(httpCode, "Route Not Found");
        } else if (httpCode.equals(HTTPCode.METHOD_NOT_ALLOWED)) {
            data = new ErrorMessage(httpCode, "Method For This Route Is Incorrect");
        } else {
            data = new ErrorMessage(httpCode, "Unknown Error Occurred Outside Main API");
        }

        return ResponseEntity
                .status(httpCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}