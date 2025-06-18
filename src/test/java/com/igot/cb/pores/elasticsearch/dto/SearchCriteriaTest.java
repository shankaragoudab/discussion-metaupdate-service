package com.igot.cb.pores.elasticsearch.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SearchCriteriaTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        SearchCriteria searchCriteria = new SearchCriteria();

        HashMap<String, Object> filterCriteriaMap = new HashMap<>();
        filterCriteriaMap.put("key1", "value1");
        searchCriteria.setFilterCriteriaMap(filterCriteriaMap);

        List<String> requestedFields = List.of("field1", "field2");
        searchCriteria.setRequestedFields(requestedFields);

        searchCriteria.setPageNumber(1);
        searchCriteria.setPageSize(10);
        searchCriteria.setOrderBy("field1");
        searchCriteria.setOrderDirection("asc");
        searchCriteria.setSearchString("testSearch");
        searchCriteria.setFacets(List.of("facet1", "facet2"));

        Map<String, Object> query = Map.of("queryKey", "queryValue");
        searchCriteria.setQuery(query);

        assertEquals(filterCriteriaMap, searchCriteria.getFilterCriteriaMap());
        assertEquals(requestedFields, searchCriteria.getRequestedFields());
        assertEquals(1, searchCriteria.getPageNumber());
        assertEquals(10, searchCriteria.getPageSize());
        assertEquals("field1", searchCriteria.getOrderBy());
        assertEquals("asc", searchCriteria.getOrderDirection());
        assertEquals("testSearch", searchCriteria.getSearchString());
        assertEquals(List.of("facet1", "facet2"), searchCriteria.getFacets());
        assertEquals(query, searchCriteria.getQuery());
    }

    @Test
    void testAllArgsConstructor() {
        HashMap<String, Object> filterCriteriaMap = new HashMap<>();
        filterCriteriaMap.put("key1", "value1");

        List<String> requestedFields = List.of("field1", "field2");
        List<String> facets = List.of("facet1", "facet2");
        Map<String, Object> query = Map.of("queryKey", "queryValue");

        SearchCriteria searchCriteria = new SearchCriteria(
                filterCriteriaMap,
                requestedFields,
                1,
                10,
                "field1",
                "asc",
                "testSearch",
                facets,
                query
        );

        assertEquals(filterCriteriaMap, searchCriteria.getFilterCriteriaMap());
        assertEquals(requestedFields, searchCriteria.getRequestedFields());
        assertEquals(1, searchCriteria.getPageNumber());
        assertEquals(10, searchCriteria.getPageSize());
        assertEquals("field1", searchCriteria.getOrderBy());
        assertEquals("asc", searchCriteria.getOrderDirection());
        assertEquals("testSearch", searchCriteria.getSearchString());
        assertEquals(facets, searchCriteria.getFacets());
        assertEquals(query, searchCriteria.getQuery());
    }
}