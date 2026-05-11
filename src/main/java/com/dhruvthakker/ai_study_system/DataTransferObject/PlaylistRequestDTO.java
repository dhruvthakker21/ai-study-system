package com.dhruvthakker.ai_study_system.DataTransferObject;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistRequestDTO {

    @NotBlank
    String playlistUrl;

    String language="English";
}
