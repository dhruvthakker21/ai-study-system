package com.dhruvthakker.ai_study_system.services;

// Standard Spring and Jakarta imports
import com.dhruvthakker.ai_study_system.model.*;
import com.dhruvthakker.ai_study_system.repository.PlaylistNotesRepo;
import com.dhruvthakker.ai_study_system.repository.PlaylistRepo;
import com.dhruvthakker.ai_study_system.repository.SummaryRepo;
import com.dhruvthakker.ai_study_system.repository.VideoRepo;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// Jackson JSON imports
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Java Utility imports
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    @Autowired
    PlaylistRepo plr;

    @Autowired
    VideoRepo vr;

    @Autowired
    SummaryRepo sr;

    @Autowired
    PlaylistNotesRepo pnr;

    @Autowired
    @Qualifier("youtubeWebClient")
    WebClient youtubeWebClient;

    @Value("${youtube.api.key}")
    String apiKey;

    @Autowired
    @Qualifier("pythonWebClient")
    WebClient pythonWebClient;

    // CHANGE 1: Removed huggingfaceWebClient injection, replaced with Groq API key
    @Value("${groq.api.key}")
    String groqApiKey;

    @Transactional
    public String analyzePlaylist(String playlistUrl,String language) throws Exception {

        if (playlistUrl == null || !playlistUrl.contains("list=")) {
            throw new IllegalArgumentException("Invalid YouTube playlist URL");
        }

        String rawId = playlistUrl.substring(playlistUrl.indexOf("list=") + 5);
        String playlistId = rawId.contains("&") ? rawId.substring(0, rawId.indexOf("&")) : rawId;

        System.out.println(">>> PLAYLIST ID: [" + playlistId + "]");

        if (plr.existsByPlaylistId(playlistId)) {
            System.out.println(">>> Playlist already exists in DB. Skipping.");
            return playlistId;
        }

        String response = youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/playlistItems")
                        .queryParam("part", "snippet")
                        .queryParam("playlistId", playlistId)
                        .queryParam("maxResults", 50)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper obm = new ObjectMapper();
        JsonNode root = obm.readTree(response);
        JsonNode items = root.path("items");
        JsonNode playlistInfo = root.path("items").get(0).path("snippet");

        String channelTitle = playlistInfo.path("channelTitle").asText();
        String playlistTitle = playlistInfo.path("title").asText();

        Playlist playlist = new Playlist();
        playlist.setPlaylistId(playlistId);
        playlist.setStatus(ProcessingStatus.PENDING);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setChannelTitle(channelTitle);
        playlist.setTitle(playlistTitle);

        List<Video> videoList = new ArrayList<>();

        for (JsonNode item : items) {
            String videoId = item.path("snippet").path("resourceId").path("videoId").asText();
            String title = item.path("snippet").path("title").asText();
            String desc = item.path("snippet").path("description").asText();

            Video video = new Video();
            video.setVideoId(videoId);
            video.setTitle(title);
            video.setStatus(ProcessingStatus.PENDING);
            video.setCreatedAt(LocalDateTime.now());
            video.setPlaylist(playlist);
            video.setDescription(desc);

            videoList.add(video);

        }

        plr.save(playlist);
        playlist.setStatus(ProcessingStatus.IN_PROGRESS);
        plr.save(playlist);
        vr.saveAll(videoList);

        for (Video video : videoList) {
            try {
                Thread.sleep(5000);
                String transcriptResponse = pythonWebClient.post()
                        .uri("/transcript")
                        .bodyValue(Map.of("videoId", video.getVideoId()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode transcriptNode = obm.readTree(transcriptResponse);
                String transcript = transcriptNode.path("transcript").asText();
                video.setTranscriptText(transcript);
                vr.save(video);

                // CHANGE 2: Increased truncation limit from 3000 to 5000 chars
                // Groq supports larger context than HuggingFace
                String inputText = transcript.length() > 5000
                        ? transcript.substring(0, 5000)
                        : transcript;

// FIXED: Use ObjectMapper to build body safely instead of raw string
// Raw string breaks when transcript contains quotes or special characters
                Map<String, Object> groqBody = Map.of(
                        "model", "llama-3.3-70b-versatile",
                        "max_tokens", 300,
                        "messages", List.of(
                                Map.of("role", "user",
                                        "content", "Summarize the following content in " + language + " language, in 3-4 sentences: " + inputText)
                        )
                );

                String groqResponse = WebClient.builder()
                        .baseUrl("https://api.groq.com/openai/v1")
                        .defaultHeader("Authorization", "Bearer " + groqApiKey)
                        .defaultHeader("Content-Type", "application/json")
                        .build()
                        .post()
                        .uri("/chat/completions")
                        .bodyValue(groqBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode groqNode = obm.readTree(groqResponse);
                String summaryText = groqNode.path("choices").get(0)
                        .path("message").path("content").asText();

                Summary summary = new Summary();
                summary.setSummaryText(summaryText);
                summary.setVideo(video);
                summary.setCreatedAt(LocalDateTime.now());
                sr.save(summary);


            } catch (Exception e) {
                // Print full error including response body if it's a WebClient error
                if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wcEx) {
                    System.out.println(">>> Failed for: " + video.getVideoId()
                            + " | Status: " + wcEx.getStatusCode()
                            + " | Body: " + wcEx.getResponseBodyAsString());
                } else {
                    System.out.println(">>> Failed for: " + video.getVideoId() + " | " + e.getMessage());
                }

            }
        }


        playlist.setStatus(ProcessingStatus.DONE);
        plr.save(playlist);
        return playlistId;
    }
    public String generateCombinedNotes(Long playlistId) {

        Playlist playlist = plr.findById(playlistId).orElseThrow();
        List<Video> videos = vr.findByPlaylist(playlist);

        StringBuilder combined = new StringBuilder();
        combined.append("Playlist: ").append(playlist.getTitle()).append("\n\n");

        for (Video video : videos) {
            Summary summary = sr.findByVideo(video);
            combined.append("Video: ").append(video.getTitle()).append("\n");
            if (summary != null) {
                combined.append(summary.getSummaryText()).append("\n\n");
            }
        }

        PlaylistNotes notes = pnr.findByPlaylistId(playlistId);
        if (notes == null) {
            notes = new PlaylistNotes();
        }
        notes.setPlaylistId(playlistId);
        notes.setCombineNotes(combined.toString());
        pnr.save(notes);

        return combined.toString();
    }

    public String getplaylistStatus(Long id) {
        Playlist playlist=plr.findById(id).orElseThrow();
        return playlist.getStatus().toString();
    }

    public byte[] exportNotes(Long playlistid, String format) throws Exception {
        PlaylistNotes playlistNotes = pnr.findByPlaylistId(playlistid);
        if (playlistNotes == null) {
            generateCombinedNotes(playlistid);
            playlistNotes = pnr.findByPlaylistId(playlistid);
        }
        String combinenotes=playlistNotes.getCombineNotes();
        if(format.equals("md")){
            return combinenotes.getBytes();
        }
        else{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, stream);
            document.open();
            document.add(new Paragraph(combinenotes));
            document.close();
            return stream.toByteArray();
        }
    }

    public String analyzeVideo(String videoUrl, String language) throws Exception {
        String videoId;
        if (videoUrl.contains("v=")) {
            videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
        } else {
            videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
        }
        if (videoId.contains("&")) {
            videoId = videoId.substring(0, videoId.indexOf("&"));
        }
        if (videoId.contains("?")) {
            videoId = videoId.substring(0, videoId.indexOf("?"));
        }

        ObjectMapper obm = new ObjectMapper();

        String transcriptResponse = pythonWebClient.post()
                .uri("/transcript")
                .bodyValue(Map.of("videoId", videoId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode transcriptNode = obm.readTree(transcriptResponse);
        String transcript = transcriptNode.path("transcript").asText();

        String inputText = transcript.length() > 5000
                ? transcript.substring(0, 5000)
                : transcript;

        Map<String, Object> groqBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "max_tokens", 300,
                "messages", List.of(
                        Map.of("role", "user",
                                "content", "Summarize the following content in " + language + " language, in 3-4 sentences: " + inputText)
                )
        );

        String groqResponse = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build()
                .post()
                .uri("/chat/completions")
                .bodyValue(groqBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode groqNode = obm.readTree(groqResponse);
        String summaryText = groqNode.path("choices").get(0)
                .path("message").path("content").asText();

        Video video = new Video();
        video.setVideoId(videoId);
        video.setTitle(videoId);
        video.setStatus(ProcessingStatus.DONE);
        video.setCreatedAt(LocalDateTime.now());
        video.setTranscriptText(transcript);
        vr.save(video);

        Summary summary = new Summary();
        summary.setSummaryText(summaryText);
        summary.setVideo(video);
        summary.setCreatedAt(LocalDateTime.now());
        sr.save(summary);

        return summaryText;
    }
}