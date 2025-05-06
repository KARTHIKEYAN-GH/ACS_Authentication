package com.acs.authentication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<JsonNode> handleUnauthorized(UnauthorizedException ex) {
//        ObjectNode errorJson = objectMapper.createObjectNode();
//        errorJson.put("error", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorJson);
//    }
}
