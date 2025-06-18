package com.igot.cb.pores.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionTest {

    @Test
    void testNoArgsConstructor() {
        CustomException exception = new CustomException();

        assertNull(exception.getCode());
        assertNull(exception.getMessage());
        assertNull(exception.getHttpStatusCode());
    }

    @Test
    void testAllArgsConstructor() {
        String code = "ERR001";
        String message = "Test error message";
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        CustomException exception = new CustomException(code, message, httpStatus);

        assertEquals(code, exception.getCode());
        assertEquals(message, exception.getMessage());
        assertEquals(httpStatus, exception.getHttpStatusCode());
    }

    @Test
    void testSettersAndGetters() {
        CustomException exception = new CustomException();

        exception.setCode("ERR002");
        exception.setMessage("Another test error message");
        exception.setHttpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        assertEquals("ERR002", exception.getCode());
        assertEquals("Another test error message", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatusCode());
    }
}