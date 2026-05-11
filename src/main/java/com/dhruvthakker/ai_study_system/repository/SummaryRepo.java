package com.dhruvthakker.ai_study_system.repository;

import com.dhruvthakker.ai_study_system.model.Summary;
import com.dhruvthakker.ai_study_system.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepo extends JpaRepository<Summary,Long> {

    Summary findByVideo(Video video);
}
