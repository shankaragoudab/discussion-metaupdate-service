package com.igot.cb.pores.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testBuilderAndGetters() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("ERR001")
                .message("Test error message")
                .httpStatusCode(400)
                .build();

        assertEquals("ERR001", errorResponse.getCode());
        assertEquals("Test error message", errorResponse.getMessage());
        assertEquals(400, errorResponse.getHttpStatusCode());
    }

    @Test
    void testImmutability() {
        ErrorResponse errorResponse1 = ErrorResponse.builder()
                .code("ERR001")
                .message("Test error message")
                .httpStatusCode(400)
                .build();

        ErrorResponse errorResponse2 = ErrorResponse.builder()
                .code("ERR002")
                .message("Another error message")
                .httpStatusCode(500)
                .build();

        assertNotEquals(errorResponse1, errorResponse2);
    }
}