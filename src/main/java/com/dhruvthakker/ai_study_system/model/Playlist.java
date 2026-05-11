package com.dhruvthakker.ai_study_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="playlist")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Playlist {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;
    String playlistId;
    String title;
    String channelTitle;
    Integer videoCount;

    @Enumerated(EnumType.STRING)
    ProcessingStatus status;
    LocalDateTime createdAt;
}
