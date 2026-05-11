package com.dhruvthakker.ai_study_system.repository;

import com.dhruvthakker.ai_study_system.model.PlaylistNotes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistNotesRepo extends JpaRepository<PlaylistNotes,Integer> {

    PlaylistNotes findByPlaylistId(Long playlistId);
}
