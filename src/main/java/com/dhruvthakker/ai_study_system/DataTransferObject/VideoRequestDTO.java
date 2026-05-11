package com.dhruvthakker.ai_study_system.DataTransferObject;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoRequestDTO {

    @NotBlank
    String videoUrl;

    String language="English";
}
