package com.dhruvthakker.ai_study_system.controller;


import com.dhruvthakker.ai_study_system.DataTransferObject.PlaylistRequestDTO;
import com.dhruvthakker.ai_study_system.DataTransferObject.VideoRequestDTO;
import com.dhruvthakker.ai_study_system.services.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PlayListController {


    @Autowired
    PlaylistService pls;

    @Operation(summary = "Analyze a YouTube playlist", description = "Fetches playlist metadata, transcripts and generates AI summaries")
    @PostMapping("/analyze-playlist")
    public ResponseEntity<String> analyzePlaylist(@RequestBody PlaylistRequestDTO request) throws Exception {
        String result = pls.analyzePlaylist(request.getPlaylistUrl(), request.getLanguage());
        return ResponseEntity.accepted().body(result);
    }

    @Operation(summary = "Get combined notes", description = "Returns AI generated combined study notes for a playlist")
    @GetMapping("/playlist/{id}/notes")
    public ResponseEntity<String> generateCombineNotes(@PathVariable Long id){
        String result=pls.generateCombinedNotes(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Shows status of your video or playlist")
    @GetMapping("/playlist/{id}/status")
    public ResponseEntity<String> getplaylistStatus(@PathVariable Long id){
        String result=pls.getplaylistStatus(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Save file in your device", description = "Returns AI generated combined study notes for a playlist and single video")
    @GetMapping("/playlist/{id}/export")
    public ResponseEntity<byte[]> exportNotes(@PathVariable Long id,@RequestParam String format) throws Exception{
        byte[] result=pls.exportNotes(id,format);
        String filename = format.equals("pdf") ? "notes.pdf" : "notes.md";
        String contentType = format.equals("pdf") ? "application/pdf" : "text/markdown";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(result);
    }

    @Operation(summary = "Analyze single video", description = "Fetch dat from single video")
    @PostMapping("/analyze-video")
    public ResponseEntity<String> analyzeVideo(@RequestBody VideoRequestDTO request) throws Exception{
        String result=pls.analyzeVideo(request.getVideoUrl(),request.getLanguage());
        return ResponseEntity.accepted().body(result);
    }

}