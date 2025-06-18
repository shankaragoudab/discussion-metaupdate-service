package com.igot.cb.pores.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheServiceTest {

  private CacheService cacheService;

  @Mock
  private JedisPool mockJedisPool;

  @Mock
  private Jedis mockJedis;

  @Mock
  private ObjectMapper mockObjectMapper;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    cacheService = new CacheService();

    // Set the private field 'jedisPool'
    Field jedisPoolField = CacheService.class.getDeclaredField("jedisPool");
    jedisPoolField.setAccessible(true);
    jedisPoolField.set(cacheService, mockJedisPool);

    // Set the private field 'objectMapper'
    Field objectMapperField = CacheService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(cacheService, mockObjectMapper);

    // Set the private field 'cacheTtl'
    Field cacheTtlField = CacheService.class.getDeclaredField("cacheTtl");
    cacheTtlField.setAccessible(true);
    cacheTtlField.set(cacheService, 3600L); // Example TTL value

    when(mockJedisPool.getResource()).thenReturn(mockJedis);
  }

  @Test
  void testPutCache() throws Exception {
    String key = "testKey";
    Object value = new Object();
    String serializedValue = "{\"key\":\"value\"}";

    when(mockObjectMapper.writeValueAsString(value)).thenReturn(serializedValue);

    cacheService.putCache(key, value);

    verify(mockJedis).set(Constants.REDIS_KEY_PREFIX + key, serializedValue);
    verify(mockJedis).expire(Constants.REDIS_KEY_PREFIX + key, 3600L);
  }

  @Test
  void testGetCache() {
    String key = "testKey";
    String expectedValue = "cachedValue";

    when(mockJedis.get(Constants.REDIS_KEY_PREFIX + key)).thenReturn(expectedValue);

    String actualValue = cacheService.getCache(key);

    assertEquals(expectedValue, actualValue);
  }

  @Test
  void testDeleteCache() {
    String key = "testKey";

    when(mockJedis.del(Constants.REDIS_KEY_PREFIX + key)).thenReturn(1L);

    Long result = cacheService.deleteCache(key);

    assertEquals(1L, result);
    verify(mockJedis).del(Constants.REDIS_KEY_PREFIX + key);
  }

  @Test
  void testUpsertUserToHash() {
    String key = "testHash";
    String field = "testField";
    String value = "testValue";

    when(mockJedis.hset(key, field, value)).thenReturn(1L);

    cacheService.upsertUserToHash(key, field, value);

    verify(mockJedis).hset(key, field, value);
  }
}