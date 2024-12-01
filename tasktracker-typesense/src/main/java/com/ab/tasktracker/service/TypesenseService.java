package com.ab.tasktracker.service;

import com.ab.tasktracker.config.TypesenseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;
import org.typesense.model.SearchParameters;
import org.typesense.model.SearchResult;
import org.typesense.model.SearchResultHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to interact with Type sense collections
 */
@Component
public class TypesenseService {

    Logger LOGGER = LoggerFactory.getLogger(TypesenseService.class);

    @Autowired
    private TypesenseConfig typesenseConfig;

    /**
     * Indexes documents into collection
     *
     * @param collectionName for example - task_collection
     * @param documentsMap   data map
     */
    public void insertDataToCollection(String collectionName, Map<String, Object> documentsMap) throws Exception {
        Client client = typesenseConfig.getTypeSenseClient();
        client.collections(collectionName).documents().create(documentsMap);
    }

    /**
     * Update documents into collection
     *
     * @param collectionName      for example - task_collection
     * @param documentsMap        data map
     * @param defaultSortingField used to find document
     */
    public void updateDataToCollection(String collectionName, Map<String, Object> documentsMap, String defaultSortingField) throws Exception {
        Client client = typesenseConfig.getTypeSenseClient();
        LOGGER.debug("Document with taskId={} updated successfully", defaultSortingField);

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.q(defaultSortingField);
        searchParameters.queryBy("taskId");
        SearchResult search = client.collections(collectionName).documents().search(searchParameters);
        if (search.getFound() > 0) {
            Map<String,Object> map = search.getHits().getFirst().getDocument();
            Map<String,Object> Updatedmap = client.collections(collectionName).documents(map.get("id").toString()).update(documentsMap);
            LOGGER.debug("Updated document {}", Updatedmap);
        } else {
            LOGGER.debug("No document found");
        }
    }

    /**
     * Get Single document from collection
     *
     * @param collectionName      for example - task_collection
     * @param defaultSortingField used to find document
     * @return Map<String, Object>
     */
    public Map<String, Object> getSingleDataFromCollection(String collectionName, String defaultSortingField) throws Exception {
        Client client = new TypesenseConfig().getTypeSenseClient();
        Map<String, Object> documentsMap = client.collections(collectionName).documents(defaultSortingField).retrieve();
        return documentsMap;
    }

    /**
     * Get all documents of currently logged in user
     *
     * @param collectionName for example - task_collection
     * @return CollectionResponse
     */
    public List<Map<String,Object>> getAllDocumentsData(String collectionName, Long userId) throws Exception {
        List<Map<String,Object>> resultListMap = new ArrayList<>();
        Client client = typesenseConfig.getTypeSenseClient();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.q(userId.toString());
        searchParameters.queryBy("assignee");
        List<SearchResultHit> searchResultHits = client.collections(collectionName).documents().search(searchParameters).getHits();
        searchResultHits.forEach(i -> resultListMap.add(i.getDocument()));
        return resultListMap;
    }
}
