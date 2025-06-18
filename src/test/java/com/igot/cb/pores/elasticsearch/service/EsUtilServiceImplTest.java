package com.igot.cb.pores.elasticsearch.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.pores.elasticsearch.dto.FacetDTO;
import com.igot.cb.pores.elasticsearch.dto.SearchCriteria;
import com.igot.cb.pores.elasticsearch.dto.SearchResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EsUtilServiceImplTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private EsUtilServiceImpl esUtilService;

    @BeforeEach
    public void setUp() throws Exception {
        // Create service with mocked Elasticsearch client
        esUtilService = new EsUtilServiceImpl(elasticsearchClient);

        // Set the ObjectMapper using reflection
        Field field = EsUtilServiceImpl.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(esUtilService, objectMapper);
    }

    private void setupElasticsearchMock(Result indexResult) throws IOException {
        // Mock IndexResponse
        IndexResponse mockResponse = mock(IndexResponse.class);
        when(mockResponse.result()).thenReturn(indexResult);

        // Mock elasticsearchClient.index
        when(elasticsearchClient.index(any(IndexRequest.class))).thenReturn(mockResponse);
    }

    @Test
    public void testAddDocument_Success() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField1", "value1");
        document.put("validField2", 123);
        document.put("invalidField", "value3"); // Should be removed

        setupElasticsearchMock(Result.Created);

        // Act
        String result = esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Successfully indexed document"));

        // Verify elasticsearch client was called
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    public void testAddDocument_InvalidSchemaPath() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/non-existent-schema.json"; // Non-existent path
        Map<String, Object> document = new HashMap<>();

        // Act
        String result = esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNull(result);
        verify(elasticsearchClient, never()).index(any(IndexRequest.class));
    }

    @Test
    public void testAddDocument_ElasticsearchException() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField1", "value1");

        // Mock elasticsearchClient.index to throw exception
        when(elasticsearchClient.index(any(IndexRequest.class)))
                .thenThrow(new RuntimeException("Elasticsearch connection error"));

        // Act
        String result = esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNull(result);
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    public void testAddDocument_EmptyDocument() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        setupElasticsearchMock(Result.Created);

        // Act
        String result = esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Successfully indexed document"));
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    public void testAddDocument_NullDocument() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        // Act
        String result = esUtilService.addDocument(esIndexName, id, null, jsonFilePath);

        // Assert
        assertNull(result);
        verify(elasticsearchClient, never()).index(any(IndexRequest.class));
    }

    @Test
    public void testAddDocument_VerifyIndexRequestDetails() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField", "value");

        setupElasticsearchMock(Result.Created);

        // Act
        esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert with ArgumentCaptor
        ArgumentCaptor<IndexRequest<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(IndexRequest.class);
        verify(elasticsearchClient).index(requestCaptor.capture());

        IndexRequest<Map<String, Object>> capturedRequest = requestCaptor.getValue();
        assertEquals(esIndexName, capturedRequest.index());
        assertEquals(id, capturedRequest.id());
        assertEquals(Refresh.True, capturedRequest.refresh());
    }

    @Test
    public void testAddDocument_ComplexSchemaFiltering() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/complex-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("field1", "value1");
        document.put("field2", 123);
        document.put("field3", true);
        document.put("field4", new HashMap<>());
        document.put("field5", "remove me"); // Should be removed

        setupElasticsearchMock(Result.Created);

        // Act
        String result = esUtilService.addDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNotNull(result);
        // We expect field5 to be filtered out as it's not in the schema
        assertFalse(document.containsKey("field5"));
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    public void testUpdateDocument_Success() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField1", "value1");
        document.put("validField2", 123);
        document.put("invalidField", "value3"); // Should be removed

        setupElasticsearchMock(Result.Updated);

        // Act
        String result = esUtilService.updateDocument(esIndexName, id, document, jsonFilePath);

        // Assert
        assertNotNull(result);
        assertEquals("updated", result);

        // Verify document was modified (invalidField was removed)
        assertFalse(document.containsKey("invalidField"));

        // Verify elasticsearch client was called with correct parameters
        ArgumentCaptor<IndexRequest<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(IndexRequest.class);
        verify(elasticsearchClient).index(requestCaptor.capture());

        IndexRequest<Map<String, Object>> capturedRequest = requestCaptor.getValue();
        assertEquals(esIndexName, capturedRequest.index());
        assertEquals(id, capturedRequest.id());
        assertEquals(Refresh.True, capturedRequest.refresh());
    }

    @Test
    public void testUpdateDocument_InvalidSchemaPath() {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/non-existent-schema.json"; // Non-existent path
        Map<String, Object> document = new HashMap<>();

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            esUtilService.updateDocument(esIndexName, id, document, jsonFilePath);
        });

        // Just checking that some exception occurred, not the exact message
        assertNotNull(exception.getMessage());
        verifyNoInteractions(elasticsearchClient);
    }

    @Test
    public void testUpdateDocument_ElasticsearchException() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField1", "value1");

        // Mock elasticsearchClient.index to throw exception
        when(elasticsearchClient.index(any(IndexRequest.class)))
                .thenThrow(new IOException("Elasticsearch connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            esUtilService.updateDocument(esIndexName, id, document, jsonFilePath);
        });

        assertTrue(exception.getMessage().contains("Error occured while updating es index"));
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    public void testUpdateDocument_SchemaFiltering() throws Exception {
        // Arrange
        String esIndexName = "test-index";
        String id = "doc-123";
        String jsonFilePath = "/schemas/test-schema.json";

        Map<String, Object> document = new HashMap<>();
        document.put("validField1", "value1");
        document.put("validField2", 123);
        document.put("invalidField", "value3"); // Should be removed

        setupElasticsearchMock(Result.Updated);

        // Act
        esUtilService.updateDocument(esIndexName, id, document, jsonFilePath);

        // Assert - check if document was filtered correctly
        assertEquals(2, document.size());
        assertTrue(document.containsKey("validField1"));
        assertTrue(document.containsKey("validField2"));
        assertFalse(document.containsKey("invalidField"));
    }

    @Test
    public void testDeleteDocument_Success() throws IOException {
        // Arrange
        String documentId = "doc-123";
        String esIndexName = "test-index";

        // Mock delete response
        DeleteResponse mockResponse = mock(DeleteResponse.class);
        Result result = mock(Result.class);
        when(result.jsonValue()).thenReturn("DELETED");
        when(mockResponse.result()).thenReturn(result);
        when(elasticsearchClient.delete(any(DeleteRequest.class))).thenReturn(mockResponse);

        // Mock indices client - use ElasticsearchIndicesClient instead of IndicesClient
        ElasticsearchIndicesClient mockIndicesClient = mock(ElasticsearchIndicesClient.class);
        when(elasticsearchClient.indices()).thenReturn(mockIndicesClient);

        // Act
        esUtilService.deleteDocument(documentId, esIndexName);

        // Assert
        verify(elasticsearchClient).delete(any(DeleteRequest.class));
        verify(mockIndicesClient).refresh(any(RefreshRequest.class));
    }

    @Test
    public void testDeleteDocument_NotFound() throws IOException {
        // Arrange
        String documentId = "doc-123";
        String esIndexName = "test-index";

        // Mock delete response for not found
        DeleteResponse mockResponse = mock(DeleteResponse.class);
        Result result = mock(Result.class);
        when(result.jsonValue()).thenReturn("NOT_FOUND");
        when(mockResponse.result()).thenReturn(result);
        when(elasticsearchClient.delete(any(DeleteRequest.class))).thenReturn(mockResponse);

        // Act
        esUtilService.deleteDocument(documentId, esIndexName);

        // Assert
        verify(elasticsearchClient).delete(any(DeleteRequest.class));
        verify(elasticsearchClient, never()).indices();
    }

    @Test
    public void testDeleteDocument_Exception() throws IOException {
        // Arrange
        String documentId = "doc-123";
        String esIndexName = "test-index";

        // Mock delete to throw exception
        when(elasticsearchClient.delete(any(DeleteRequest.class)))
                .thenThrow(new IOException("Connection error"));

        // Act
        esUtilService.deleteDocument(documentId, esIndexName);

        // Assert
        verify(elasticsearchClient).delete(any(DeleteRequest.class));
        verify(elasticsearchClient, never()).indices();
    }

    @Test
    public void testDeleteDocument_VerifyRequestParameters() throws IOException {
        // Arrange
        String documentId = "doc-123";
        String esIndexName = "test-index";

        // Mock delete response
        DeleteResponse mockResponse = mock(DeleteResponse.class);
        Result result = mock(Result.class);
        when(result.jsonValue()).thenReturn("DELETED");
        when(mockResponse.result()).thenReturn(result);
        when(elasticsearchClient.delete(any(DeleteRequest.class))).thenReturn(mockResponse);

        // Mock indices client
        ElasticsearchIndicesClient mockIndicesClient = mock(ElasticsearchIndicesClient.class);
        when(elasticsearchClient.indices()).thenReturn(mockIndicesClient);

        // Act
        esUtilService.deleteDocument(documentId, esIndexName);

        // Assert with ArgumentCaptor
        ArgumentCaptor<DeleteRequest> requestCaptor = ArgumentCaptor.forClass(DeleteRequest.class);
        verify(elasticsearchClient).delete(requestCaptor.capture());

        DeleteRequest capturedRequest = requestCaptor.getValue();
        assertEquals(esIndexName, capturedRequest.index());
        assertEquals(documentId, capturedRequest.id());
    }

    @Test
    public void testSearchDocuments_Success() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setPageSize(10);
        searchCriteria.setFilterCriteriaMap(new HashMap<>());

        // Mock search response
        SearchResponse<Object> mockResponse = mock(SearchResponse.class);
        HitsMetadata<Object> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(5L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        // Mock hits
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Hit<Object> hit = mock(Hit.class);
            Map<String, Object> source = new HashMap<>();
            source.put("field" + i, "value" + i);
            when(hit.source()).thenReturn(source);
            hits.add(hit);
        }
        when(hitsMetadata.hits()).thenReturn(hits);
        when(mockResponse.hits()).thenReturn(hitsMetadata);
        when(mockResponse.aggregations()).thenReturn(null);

        // Mock elasticsearch client
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(mockResponse);

        // Act
        SearchResult result = esUtilService.searchDocuments(esIndexName, searchCriteria);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getData().size());
        assertEquals(5L, result.getTotalCount());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
    }

    @Test
    public void testSearchDocuments_WithFacets() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(0);
        searchCriteria.setPageSize(10);
        searchCriteria.setFacets(List.of("category"));
        searchCriteria.setFilterCriteriaMap(new HashMap<>());

        // Mock search response
        SearchResponse<Object> mockResponse = mock(SearchResponse.class);
        HitsMetadata<Object> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(3L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        // Mock hits
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Hit<Object> hit = mock(Hit.class);
            Map<String, Object> source = new HashMap<>();
            source.put("field" + i, "value" + i);
            when(hit.source()).thenReturn(source);
            hits.add(hit);
        }
        when(hitsMetadata.hits()).thenReturn(hits);
        when(mockResponse.hits()).thenReturn(hitsMetadata);

        // Mock aggregations properly
        Map<String, Aggregate> aggregations = new HashMap<>();
        Aggregate aggregate = mock(Aggregate.class);
        StringTermsAggregate sterms = mock(StringTermsAggregate.class);
        when(aggregate.isSterms()).thenReturn(true);
        when(aggregate.sterms()).thenReturn(sterms);

        List<StringTermsBucket> bucketList = new ArrayList<>();
        StringTermsBucket bucket = mock(StringTermsBucket.class);
        FieldValue fieldValue = mock(FieldValue.class);
        when(fieldValue.stringValue()).thenReturn("category1");
        when(bucket.key()).thenReturn(fieldValue);
        when(bucket.docCount()).thenReturn(2L);
        bucketList.add(bucket);

        Buckets<StringTermsBucket> buckets = mock(Buckets.class);
        when(buckets.array()).thenReturn(bucketList);
        when(sterms.buckets()).thenReturn(buckets);

        aggregations.put("category_agg", aggregate);
        when(mockResponse.aggregations()).thenReturn(aggregations);

        // Mock elasticsearch client
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(mockResponse);

        // Act
        SearchResult result = esUtilService.searchDocuments(esIndexName, searchCriteria);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getData().size());
        assertEquals(3L, result.getTotalCount());
        assertNotNull(result.getFacets());
        assertFalse(result.getFacets().isEmpty());
    }

    @Test
    public void testSearchDocuments_Pagination() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPageNumber(1); // Second page
        searchCriteria.setPageSize(5);
        searchCriteria.setFilterCriteriaMap(new HashMap<>());

        // Mock search response
        SearchResponse<Object> mockResponse = mock(SearchResponse.class);
        HitsMetadata<Object> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(15L); // Total 15 results
        when(hitsMetadata.total()).thenReturn(totalHits);

        // Mock hits for second page
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Hit<Object> hit = mock(Hit.class);
            Map<String, Object> source = new HashMap<>();
            source.put("field1", "value" + (i + 5)); // Values 5-9
            when(hit.source()).thenReturn(source);
            hits.add(hit);
        }
        when(hitsMetadata.hits()).thenReturn(hits);
        when(mockResponse.hits()).thenReturn(hitsMetadata);
        when(mockResponse.aggregations()).thenReturn(new HashMap<>());

        // Mock elasticsearch client
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(mockResponse);

        // Act
        SearchResult result = esUtilService.searchDocuments(esIndexName, searchCriteria);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getData().size());
        assertEquals(15L, result.getTotalCount());

        // Verify pagination parameters
        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(elasticsearchClient).search(requestCaptor.capture(), eq(Object.class));

        // Page 1 with size 5 should start at index 5
        assertEquals(Integer.valueOf(5), requestCaptor.getValue().from());
        assertEquals(Integer.valueOf(5), requestCaptor.getValue().size());
    }

    @Test
    public void testSearchDocuments_Exception() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setFilterCriteriaMap(new HashMap<>());

        // Mock elasticsearch client to throw exception
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class)))
                .thenThrow(new IOException("Connection error"));

        // Act
        SearchResult result = esUtilService.searchDocuments(esIndexName, searchCriteria);

        // Assert
        assertNull(result);
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
    }

    @Test
    public void testSearchDocuments_SortAndFilter() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOrderBy("name");
        searchCriteria.setOrderDirection("asc");

        // Add filter criteria
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("status", "active");
        searchCriteria.setFilterCriteriaMap((HashMap<String, Object>) filterMap);

        // Mock search response
        SearchResponse<Object> mockResponse = mock(SearchResponse.class);
        HitsMetadata<Object> hitsMetadata = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(3L);
        when(hitsMetadata.total()).thenReturn(totalHits);

        // Mock hits
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Hit<Object> hit = mock(Hit.class);
            Map<String, Object> source = new HashMap<>();
            source.put("name", "name" + i);
            source.put("status", "active");
            when(hit.source()).thenReturn(source);
            hits.add(hit);
        }
        when(hitsMetadata.hits()).thenReturn(hits);
        when(mockResponse.hits()).thenReturn(hitsMetadata);
        when(mockResponse.aggregations()).thenReturn(new HashMap<>());

        // Mock elasticsearch client
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(mockResponse);

        // Act
        SearchResult result = esUtilService.searchDocuments(esIndexName, searchCriteria);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getData().size());

        // Capture the request to verify sort and filter parameters
        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(elasticsearchClient).search(requestCaptor.capture(), eq(Object.class));

        // Verify sort was included in the request
        assertNotNull(requestCaptor.getValue().sort());
    }

    @Test
    public void testDeleteDocumentsByCriteria_Success() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        Query query = new Query.Builder()
                .match(m -> m.field("status").query(FieldValue.of("inactive")))
                .build();

        // Mock search hits
        HitsMetadata<Object> searchHits = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(3L);
        when(searchHits.total()).thenReturn(totalHits);

        // Mock hits with IDs
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Hit<Object> hit = mock(Hit.class);
            when(hit.id()).thenReturn("doc-" + i);
            hits.add(hit);
        }
        when(searchHits.hits()).thenReturn(hits);

        // Mock bulk response
        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.errors()).thenReturn(false); // No errors

        // Mock the search and bulk methods
        SearchResponse<Object> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(searchHits);
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(searchResponse);
        when(elasticsearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        // Act
        esUtilService.deleteDocumentsByCriteria(esIndexName, query);

        // Assert
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(elasticsearchClient).bulk(any(BulkRequest.class));

        // Verify bulk request contains the correct operations
        ArgumentCaptor<BulkRequest> bulkRequestCaptor = ArgumentCaptor.forClass(BulkRequest.class);
        verify(elasticsearchClient).bulk(bulkRequestCaptor.capture());
        BulkRequest capturedBulkRequest = bulkRequestCaptor.getValue();

        // Should have 3 delete operations
        assertEquals(3, capturedBulkRequest.operations().size());
    }

    @Test
    public void testDeleteDocumentsByCriteria_NoDocumentsFound() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        Query query = new Query.Builder()
                .match(m -> m.field("status").query(FieldValue.of("inactive")))
                .build();

        // Mock empty search hits
        HitsMetadata<Object> searchHits = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(0L);
        when(searchHits.total()).thenReturn(totalHits);
        when(searchHits.hits()).thenReturn(Collections.emptyList());

        // Mock search response
        SearchResponse<Object> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(searchHits);
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(searchResponse);

        // Act
        esUtilService.deleteDocumentsByCriteria(esIndexName, query);

        // Assert
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(elasticsearchClient, never()).bulk(any(BulkRequest.class));
    }

    @Test
    public void testDeleteDocumentsByCriteria_BulkResponseWithErrors() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        Query query = new Query.Builder()
                .match(m -> m.field("status").query(FieldValue.of("inactive")))
                .build();

        // Mock search hits
        HitsMetadata<Object> searchHits = mock(HitsMetadata.class);
        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(2L);
        when(searchHits.total()).thenReturn(totalHits);

        // Mock hits with IDs
        List<Hit<Object>> hits = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Hit<Object> hit = mock(Hit.class);
            when(hit.id()).thenReturn("doc-" + i);
            hits.add(hit);
        }
        when(searchHits.hits()).thenReturn(hits);

        // Mock bulk response with errors
        BulkResponse bulkResponse = mock(BulkResponse.class);
        when(bulkResponse.errors()).thenReturn(true); // Has errors

        // Mock the search and bulk methods
        SearchResponse<Object> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(searchHits);
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class))).thenReturn(searchResponse);
        when(elasticsearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        // Act
        esUtilService.deleteDocumentsByCriteria(esIndexName, query);

        // Assert
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(elasticsearchClient).bulk(any(BulkRequest.class));
    }

    @Test
    public void testDeleteDocumentsByCriteria_Exception() throws IOException {
        // Arrange
        String esIndexName = "test-index";
        Query query = new Query.Builder()
                .match(m -> m.field("status").query(FieldValue.of("inactive")))
                .build();

        // Mock search to throw exception
        when(elasticsearchClient.search(any(SearchRequest.class), eq(Object.class)))
                .thenThrow(new IOException("Connection error"));

        // Act
        esUtilService.deleteDocumentsByCriteria(esIndexName, query);

        // Assert - method should handle the exception gracefully
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(Object.class));
        verify(elasticsearchClient, never()).bulk(any(BulkRequest.class));
    }
}