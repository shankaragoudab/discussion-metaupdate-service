package com.igot.cb.pores.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropertiesCacheTest {

    private PropertiesCache propertiesCache;

    @BeforeEach
    void setUp() {
        propertiesCache = PropertiesCache.getInstance();
    }

    @Test
    void testSingletonInstance() {
        PropertiesCache instance1 = PropertiesCache.getInstance();
        PropertiesCache instance2 = PropertiesCache.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Instances should be the same (singleton)");
    }

    @Test
    void testGetProperty_WithConfigProperty() {
        Properties mockProperties = mock(Properties.class);
        when(mockProperties.getProperty("TEST_KEY")).thenReturn("configValue");

        // Use reflection to set the private field
        ReflectionTestUtils.setField(propertiesCache, "configProp", mockProperties);

        String value = propertiesCache.getProperty("TEST_KEY");

        assertEquals("configValue", value, "Should return the config property value");
    }

    @Test
    void testGetProperty_KeyNotFound() {
        String value = propertiesCache.getProperty("NON_EXISTENT_KEY");

        assertEquals("NON_EXISTENT_KEY", value, "Should return the key itself if not found");
    }


    @Test
    void testReadProperty_WithConfigProperty() {
        Properties mockProperties = mock(Properties.class);
        when(mockProperties.getProperty("TEST_KEY")).thenReturn("configValue");

        // Use reflection to set the private field
        ReflectionTestUtils.setField(propertiesCache, "configProp", mockProperties);

        String value = propertiesCache.readProperty("TEST_KEY");

        assertEquals("configValue", value, "Should return the config property value");
    }

    @Test
    void testReadProperty_KeyNotFound() {
        String value = propertiesCache.readProperty("NON_EXISTENT_KEY");

        assertNull(value, "Should return null if the key is not found");
    }
}