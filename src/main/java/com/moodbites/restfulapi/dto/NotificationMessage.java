package com.moodbites.restfulapi.dto;

import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class NotificationMessage {
    private String recipientToken;
    private String title;
    private String body;
    private String notificationType;
    private String imageUrl;
    private Map<String, String> data;
}
