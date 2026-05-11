package com.dhruvthakker.ai_study_system.repository;

import com.dhruvthakker.ai_study_system.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepo extends JpaRepository<Playlist,Long> {

    boolean existsByPlaylistId(String playlistId);

}
