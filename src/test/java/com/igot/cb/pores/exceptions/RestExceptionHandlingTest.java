package com.igot.cb.pores.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class RestExceptionHandlingTest {

    private final RestExceptionHandling exceptionHandling = new RestExceptionHandling();

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<?> responseEntity = exceptionHandling.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        ErrorResponse response = (ErrorResponse) responseEntity.getBody();
        assertNotNull(response);
        assertEquals("Something went wrong", response.getMessage());
        assertEquals("ERROR", response.getCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getHttpStatusCode());
    }

    @Test
    void testHandleCustomExceptionWithValidStatus() {
        CustomException customEx = new CustomException("INVALID_DATA", "Invalid input", HttpStatus.BAD_REQUEST);

        ResponseEntity<?> responseEntity = exceptionHandling.handleException(customEx);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ErrorResponse response = (ErrorResponse) responseEntity.getBody();
        assertNotNull(response);
        assertEquals("Invalid input", response.getMessage());
        assertEquals("INVALID_DATA", response.getCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getHttpStatusCode());
    }
}
