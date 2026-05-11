package com.dhruvthakker.ai_study_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name="videos")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String videoId;
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition ="LONGTEXT")
    String transcriptText;

    @Enumerated(EnumType.STRING)
    ProcessingStatus status;

    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "playlist_id") // This creates the column in the DB
    private Playlist playlist;

}
