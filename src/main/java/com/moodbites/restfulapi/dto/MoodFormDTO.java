package com.moodbites.restfulapi.dto;

import lombok.Setter;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.moodbites.restfulapi.dto.constructs.MoodProfile;
import com.moodbites.restfulapi.model.enums.Mood;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoodFormDTO {
    private Map<String, MoodProfile> moods;

    private List<String> VALID_MOODS = Mood.getMoods();

    public void checkDTO() {
        if (this.moods == null || this.moods.isEmpty())
            throw new IllegalArgumentException("Moods Cannot Be NULL or Empty");

        for (String mood : VALID_MOODS) {
            if (!this.moods.containsKey(mood.toLowerCase()))
                throw new IllegalArgumentException("Missing Mood: " + mood);
        }

        this.moods.forEach((moodName, detail) -> {
            if (detail == null)
                throw new IllegalArgumentException(moodName + ": MoodDetail Cannot Be NULL");
            detail.checkDTO(moodName);
        });
    }

}
