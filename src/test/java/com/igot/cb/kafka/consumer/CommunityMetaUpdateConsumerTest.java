package com.igot.cb.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.igot.cb.pores.cache.CacheService;
import com.igot.cb.pores.elasticsearch.service.EsUtilService;
import com.igot.cb.pores.entity.CommunityEntity;
import com.igot.cb.pores.repository.CommunityEngagementRepository;
import com.igot.cb.pores.util.CbServerProperties;
import com.igot.cb.pores.util.Constants;
import com.igot.cb.transactional.cassandrautils.CassandraOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class CommunityMetaUpdateConsumerTest {

    @InjectMocks
    private CommunityMetaUpdateConsumer consumer;

    @Mock
    private CommunityEngagementRepository repository;

    @Mock
    private EsUtilService esUtilService;

    @Mock
    private CbServerProperties serverProperties;

    @Mock
    private CacheService cacheService;

    @Mock
    private CassandraOperation cassandraOperation;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(consumer, "communityIndex", "test-community-index");
        ReflectionTestUtils.setField(consumer, "objectMapper", mapper);
    }

    @Test
    void testUpdatePostCount_IncrementPost() throws Exception {
        CommunityEntity entity = new CommunityEntity();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put(Constants.COUNT_OF_POST_CREATED, 2L);
        entity.setData(dataNode);
        entity.setCommunityId("community-1");

        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TYPE, Constants.POST);
        payload.put(Constants.STATUS, Constants.INCREMENT);
        payload.put(Constants.COMMUNITY_ID, "community-1");

        String payloadJson = mapper.writeValueAsString(payload);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, null, payloadJson);

        when(repository.findByCommunityIdAndIsActive("community-1", true)).thenReturn(Optional.of(entity));
        when(serverProperties.getElasticCommunityJsonPath()).thenReturn("$.data");

        consumer.upatePostCount(record);

        verify(repository).save(entity);
        verify(esUtilService).updateDocument(eq("test-community-index"), eq("community-1"), any(), any());
        verify(cacheService).putCache(eq("community-1"), any());
    }

    @Test
    void testUpdatePostCount_DecrementAnswerPost() throws Exception {
        CommunityEntity entity = new CommunityEntity();
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put(Constants.COUNT_OF_ANSWER_POST_CREATED, 5L);
        entity.setData(dataNode);
        entity.setCommunityId("community-2");

        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TYPE, Constants.ANSWER_POST);
        payload.put(Constants.STATUS, Constants.DECREMENT);
        payload.put(Constants.COMMUNITY_ID, "community-2");

        String json = mapper.writeValueAsString(payload);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, null, json);

        when(repository.findByCommunityIdAndIsActive("community-2", true)).thenReturn(Optional.of(entity));
        when(serverProperties.getElasticCommunityJsonPath()).thenReturn("$.data");

        consumer.upatePostCount(record);

        verify(repository).save(entity);
        verify(esUtilService).updateDocument(eq("test-community-index"), eq("community-2"), any(), any());
        verify(cacheService).putCache(eq("community-2"), any());
    }

    @Test
    void testUpdatePostCount_CommunityNotFound() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TYPE, Constants.POST);
        payload.put(Constants.STATUS, Constants.INCREMENT);
        payload.put(Constants.COMMUNITY_ID, "non-existent");

        String json = mapper.writeValueAsString(payload);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, null, json);

        when(repository.findByCommunityIdAndIsActive("non-existent", true)).thenReturn(Optional.empty());

        consumer.upatePostCount(record);

        verify(repository, never()).save(any());
        verify(esUtilService, never()).updateDocument(any(), any(), any(), any());
        verify(cacheService, never()).putCache(any(), any());
    }

    @Test
    void testUpdatePostCount_MissingCountField() throws Exception {
        CommunityEntity entity = new CommunityEntity();
        ObjectNode dataNode = mapper.createObjectNode(); // no count field
        entity.setData(dataNode);
        entity.setCommunityId("community-3");

        Map<String, Object> payload = new HashMap<>();
        payload.put(Constants.TYPE, Constants.POST);
        payload.put(Constants.STATUS, Constants.INCREMENT);
        payload.put(Constants.COMMUNITY_ID, "community-3");

        String json = mapper.writeValueAsString(payload);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, null, json);

        when(repository.findByCommunityIdAndIsActive("community-3", true)).thenReturn(Optional.of(entity));
        when(serverProperties.getElasticCommunityJsonPath()).thenReturn("$.data");

        consumer.upatePostCount(record);

        verify(repository).save(entity);
        verify(esUtilService).updateDocument(eq("test-community-index"), eq("community-3"), any(), any());
        verify(cacheService).putCache(eq("community-3"), any());
    }
}
