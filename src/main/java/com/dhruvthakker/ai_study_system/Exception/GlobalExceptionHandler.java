package com.dhruvthakker.ai_study_system.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Most specific first — BadRequest
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String BadInput400(Exception ex) {
        return "Invalid input: " + ex.getMessage();
    }

    // WebClient HTTP errors (Groq, YouTube API failures)
    // Previously these fell into RuntimeException → returned 404 wrongly
    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String ExternalApiError(WebClientResponseException ex) {
        return "External API error: " + ex.getStatusCode() + " | " + ex.getResponseBodyAsString();
    }

    // General runtime errors
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String RuntimeError(RuntimeException ex) {
        return "Runtime error: " + ex.getMessage();
    }

    // Catch-all
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String GeneralError500(Exception ex) {
        return "Something went wrong: " + ex.getMessage();
    }
}