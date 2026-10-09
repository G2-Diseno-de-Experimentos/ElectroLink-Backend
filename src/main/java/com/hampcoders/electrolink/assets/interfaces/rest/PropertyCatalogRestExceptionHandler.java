package com.hampcoders.electrolink.assets.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/** Return the documented 400 response directly, rather than dispatching validation errors to /error. */
@RestControllerAdvice(assignableTypes = {PropertyController.class, ComponentTypeController.class})
public class PropertyCatalogRestExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidFields(MethodArgumentNotValidException exception) {
        var field = exception.getBindingResult().getFieldError();
        var message = field == null ? "Invalid request data" : field.getField() + ": " + field.getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> malformedJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "Malformed request body"));
    }
}
