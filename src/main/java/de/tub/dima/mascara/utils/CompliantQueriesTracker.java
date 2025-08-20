package de.tub.dima.mascara.utils;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompliantQueriesTracker {
    @JsonProperty("query")
    private String query;
    @JsonProperty("compliantQueries")
    private List<CompliantQuery> compliantQueries;

    public CompliantQueriesTracker(String query) {
        this.query = query;
        compliantQueries = new ArrayList<>();
    }

    public void addCompliantQuery(String id, String compliantQuery) {
        compliantQueries.add(new CompliantQuery(id, compliantQuery));
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CompliantQuery {
        @JsonProperty("id")
        private String id;
        @JsonProperty("compliantQuery")
        private String compliantQuery;

        public CompliantQuery(String id, String compliantQuery) {
            this.id = id;
            this.compliantQuery = compliantQuery;
        }
    }
}
