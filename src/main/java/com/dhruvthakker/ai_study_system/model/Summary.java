package com.dhruvthakker.ai_study_system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition="TEXT")
    String summaryText;

    @Column(columnDefinition="TEXT")
    String keyPoints;

    LocalDateTime createdAt;
    @OneToOne
    @JoinColumn(name = "video_id")
    private Video video;
}
