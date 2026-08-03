package com.jadwal.restfulapi.dto.constructs;

import lombok.Setter;

import java.util.List;
import java.util.Optional;

import com.jadwal.restfulapi.model.enums.SampleFood;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoodProfile {
    private FlavorProfile desire;
    private FlavorProfile intensity;
    private List<String> categories;

    public void checkDTO(String moodName) {
        if (this.desire == null)
            throw new IllegalArgumentException(moodName + ": Desire Cannot Be NULL");
        if (this.intensity == null)
            throw new IllegalArgumentException(moodName + ": Intensity Cannot Be NULL");
        // if (this.categories == null || this.categories.isEmpty())
        // throw new IllegalArgumentException(moodName + ": Categories Cannot Be NULL or
        // Empty");

        trim();
        if (categories != null && !categories.isEmpty())
            checkCategories(moodName);

        this.desire.checkDTO(moodName + " [desire]");
        this.intensity.checkDTO(moodName + " [intensity]");
    }

    public void trim() {
        this.categories = Optional.ofNullable(this.categories)
                .map(list -> list.stream()
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList())
                .orElse(null);
    }

    private void checkCategories(String moodName) {
        List<String> invalid = this.categories.stream()
                .filter(c -> !SampleFood.checkExist(c))
                .toList();

        if (!invalid.isEmpty())
            throw new IllegalArgumentException(
                    moodName + ": Invalid Categories → " + invalid);
    }
}
