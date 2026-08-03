package com.moodbites.restfulapi.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessage {
    private String HTTPMessage;
    private String detail;
    private String message;

    public ErrorMessage(HTTPCode HTTPMessage, String message) {
        this.HTTPMessage = HTTPMessage.getReasonPhrase();
        this.detail = HTTPMessage.getDetailMessage();
        this.message = message;
    }

}