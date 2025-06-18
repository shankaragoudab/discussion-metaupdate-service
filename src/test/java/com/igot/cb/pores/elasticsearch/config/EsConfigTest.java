package com.igot.cb.pores.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EsConfigTest {

    private EsConfig esConfig;

    @BeforeEach
    void setUp() throws Exception {
        esConfig = new EsConfig();

        // Use reflection to inject property values
        setField(esConfig, "elasticsearchHost", "localhost");
        setField(esConfig, "elasticsearchPort", 9200);
        setField(esConfig, "elasticsearchUsername", "elastic");
        setField(esConfig, "elasticsearchPassword", "changeme");
    }

    @Test
    void testElasticsearchClientBeanCreation() {
        ElasticsearchClient client = esConfig.elasticsearchClient();
        assertNotNull(client, "ElasticsearchClient bean should not be null");
    }

    // Helper method to inject private fields
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
