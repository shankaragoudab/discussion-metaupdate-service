package com.igot.cb.pores.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class CbServerPropertiesTest {

      @Test
      void testElasticCommunityJsonPath() {
            CbServerProperties cbServerProperties = new CbServerProperties();

            // Set the value using ReflectionTestUtils
            String expectedPath = "/path/to/elastic/community.json";
            ReflectionTestUtils.setField(cbServerProperties, "elasticCommunityJsonPath", expectedPath);

            // Assert the value
            assertEquals(expectedPath, cbServerProperties.getElasticCommunityJsonPath());
      }
}