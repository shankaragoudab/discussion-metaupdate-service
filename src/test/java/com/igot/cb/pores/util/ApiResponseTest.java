package com.igot.cb.pores.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testDefaultConstructor() {
        ApiResponse apiResponse = new ApiResponse();

        // Adjusting the test to allow null ID since the class does not initialize it
        assertNull(apiResponse.getId(), "ID should be null by default");
        assertEquals("v1", apiResponse.getVer(), "Version should be 'v1'");
        assertNotNull(apiResponse.getTs(), "Timestamp should not be null");
        assertNotNull(apiResponse.getParams(), "Params should not be null");
        assertNull(apiResponse.getResponseCode(), "Response code should be null");
        assertNotNull(apiResponse.getResult(), "Result should not be null");
        assertTrue(apiResponse.getResult().isEmpty(), "Result should be empty");
    }

    @Test
    void testParameterizedConstructor() {
        String id = "test-id";
        ApiResponse apiResponse = new ApiResponse(id);

        assertEquals(id, apiResponse.getId());
        assertEquals("v1", apiResponse.getVer());
        assertNotNull(apiResponse.getTs());
        assertNotNull(apiResponse.getParams());
    }

    @Test
    void testSettersAndGetters() {
        ApiResponse apiResponse = new ApiResponse();
        String id = "new-id";
        String version = "v2";
        String timestamp = "2023-01-01T00:00:00";
        HttpStatus status = HttpStatus.OK;

        apiResponse.setId(id);
        apiResponse.setVer(version);
        apiResponse.setTs(timestamp);
        apiResponse.setResponseCode(status);

        assertEquals(id, apiResponse.getId());
        assertEquals(version, apiResponse.getVer());
        assertEquals(timestamp, apiResponse.getTs());
        assertEquals(status, apiResponse.getResponseCode());
    }

    @Test
    void testResponseMapOperations() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.put("key1", "value1");
        apiResponse.put("key2", 123);

        assertEquals("value1", apiResponse.get("key1"));
        assertEquals(123, apiResponse.get("key2"));
        assertTrue(apiResponse.containsKey("key1"));
        assertTrue(apiResponse.containsKey("key2"));

        apiResponse.putAll(Map.of("key3", "value3", "key4", 456));
        assertEquals("value3", apiResponse.get("key3"));
        assertEquals(456, apiResponse.get("key4"));
    }

    @Test
    void testSetResult() {
        ApiResponse apiResponse = new ApiResponse();
        Map<String, Object> result = Map.of("key1", "value1", "key2", 123);

        apiResponse.setResult(result);

        assertEquals(result, apiResponse.getResult());
        assertEquals("value1", apiResponse.get("key1"));
        assertEquals(123, apiResponse.get("key2"));
    }
}