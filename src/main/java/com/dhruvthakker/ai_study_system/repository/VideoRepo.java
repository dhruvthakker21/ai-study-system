package com.dhruvthakker.ai_study_system.repository;

import com.dhruvthakker.ai_study_system.model.Playlist;
import com.dhruvthakker.ai_study_system.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepo extends JpaRepository<Video,Long>{

    List<Video> findByPlaylist(Playlist playlist);
}
