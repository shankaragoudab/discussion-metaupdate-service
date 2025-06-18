package com.igot.cb.pores.elasticsearch.dto;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        SearchResult searchResult = new SearchResult();

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("key", "value");
        data.add(entry);

        Map<String, List<FacetDTO>> facets = new HashMap<>();
        facets.put("category", Collections.singletonList(new FacetDTO("label", 5L)));

        searchResult.setData(data);
        searchResult.setFacets(facets);
        searchResult.setTotalCount(1L);

        assertEquals(data, searchResult.getData());
        assertEquals(facets, searchResult.getFacets());
        assertEquals(1L, searchResult.getTotalCount());
    }

    @Test
    void testAllArgsConstructor() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("key", "value");
        data.add(entry);

        Map<String, List<FacetDTO>> facets = new HashMap<>();
        facets.put("type", Collections.singletonList(new FacetDTO("example", 10L)));

        long totalCount = 100L;

        SearchResult searchResult = new SearchResult(data, facets, totalCount);

        assertEquals(data, searchResult.getData());
        assertEquals(facets, searchResult.getFacets());
        assertEquals(totalCount, searchResult.getTotalCount());
    }
}
