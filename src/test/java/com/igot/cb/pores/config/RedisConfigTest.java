package com.igot.cb.pores.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class RedisConfigTest {

  private RedisConfig redisConfig;

  @Mock
  private JedisPoolConfig mockJedisPoolConfig;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    redisConfig = new RedisConfig();
  }

  @Test
  void testJedisPoolCreation() {
    // Mocking the Redis host and port values
    String redisHost = "localhost";
    int redisPort = 6379;

    // Setting up the mocked configuration
    when(mockJedisPoolConfig.getMaxIdle()).thenReturn(128);
    when(mockJedisPoolConfig.getMaxTotal()).thenReturn(3000);

    // Creating the JedisPool instance
    JedisPool jedisPool = redisConfig.jedisPool();

    // Verifying the JedisPool is not null
    assertNotNull(jedisPool);
  }
}
