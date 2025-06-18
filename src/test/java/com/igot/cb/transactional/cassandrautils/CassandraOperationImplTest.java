package com.igot.cb.transactional.cassandrautils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.igot.cb.pores.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CassandraOperationImplTest {

    @InjectMocks
    private CassandraOperationImpl cassandraOperation;

    @Mock
    private CassandraConnectionManager connectionManager;

    @Mock
    private CqlSession mockSession;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private BoundStatement mockBoundStatement;

    @Mock
    private ResultSet mockResultSet;

    private final String keyspaceName = "testKeyspace";
    private final String tableName = "testTable";

    @BeforeEach
    void setUp() {
        lenient().when(connectionManager.getSession(anyString())).thenReturn(mockSession);
    }

    @Test
    void testGetRecordsByPropertiesByKey_Success() {
        Map<String, Object> propertyMap = Map.of("id", 1);
        List<String> fields = List.of("id", "name");
        String key = "id";

        when(connectionManager.getSession(keyspaceName)).thenReturn(mockSession);
        when(mockSession.execute(any(SimpleStatement.class))).thenReturn(mockResultSet);

        try (MockedStatic<CassandraUtil> cassandraUtilMock = Mockito.mockStatic(CassandraUtil.class)) {
            List<Map<String, Object>> mockedResponse = List.of(Map.of("id", 1, "name", "Test"));
            cassandraUtilMock.when(() -> CassandraUtil.createResponse(mockResultSet)).thenReturn(mockedResponse);

            List<Map<String, Object>> response = cassandraOperation.getRecordsByPropertiesByKey(
                    keyspaceName, tableName, propertyMap, fields, key
            );

            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Test", response.get(0).get("name"));
        }
    }

    @Test
    void testGetRecordsByPropertiesWithoutFiltering_Success() {
        Map<String, Object> propertyMap = Map.of("id", 1);
        List<String> fields = List.of("id", "name");
        Integer limit = 10;

        when(connectionManager.getSession(keyspaceName)).thenReturn(mockSession);
        when(mockSession.execute(any(SimpleStatement.class))).thenReturn(mockResultSet);

        try (MockedStatic<CassandraUtil> cassandraUtilMock = Mockito.mockStatic(CassandraUtil.class)) {
            List<Map<String, Object>> mockedResponse = List.of(Map.of("id", 1, "name", "Test"));
            cassandraUtilMock.when(() -> CassandraUtil.createResponse(mockResultSet)).thenReturn(mockedResponse);

            List<Map<String, Object>> response = cassandraOperation.getRecordsByPropertiesWithoutFiltering(
                    keyspaceName, tableName, propertyMap, fields, limit
            );

            assertNotNull(response);
            assertEquals(1, response.size());
            assertEquals("Test", response.get(0).get("name"));
        }
    }

    @Test
    void testupdateRecord_Success() {
        // Arrange
        String keyspaceName = "testKeyspace";
        String tableName = "testTable";

        // The request map should contain the ID (primary key) and fields to update
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.ID, "123");  // Assuming Constants.ID = "id"
        request.put("name", "Updated Name");
        request.put("email", "updated@example.com");

        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        BoundStatement mockBoundStatement = mock(BoundStatement.class);

        when(connectionManager.getSession(keyspaceName)).thenReturn(mockSession);
        when(mockSession.prepare(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.bind(any(Object[].class))).thenReturn(mockBoundStatement);
        when(mockSession.execute(mockBoundStatement)).thenReturn(mockResultSet);

        // Act
        Map<String, Object> response = cassandraOperation.updateRecord(keyspaceName, tableName, request);

        // Assert
        assertEquals(Constants.SUCCESS, response.get(Constants.RESPONSE));
        verify(mockSession).execute(mockBoundStatement);
    }

    @Test
    void testLogQueryElapseTime() {
        long startTime = System.currentTimeMillis();
        String query = "SELECT * FROM testKeyspace.testTable";

        cassandraOperation.logQueryElapseTime("testOperation", startTime, query);

        // Verify logs (if using a logging framework that supports testing, e.g., LogCaptor)
    }
}