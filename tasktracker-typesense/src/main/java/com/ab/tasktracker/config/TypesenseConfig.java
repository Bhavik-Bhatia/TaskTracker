package com.ab.tasktracker.config;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.api.FieldTypes;
import org.typesense.model.CollectionSchema;
import org.typesense.model.Field;
import org.typesense.resources.Node;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public class TypesenseConfig {

    Logger LOGGER = LoggerFactory.getLogger(TypesenseConfig.class);

    private Client client;

    @Autowired
    private Environment environment;

    public void configure(String host, String port) throws Exception {
        LOGGER.debug("Type sense Connecting.....");
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("http", host, port));
        Configuration configuration = new Configuration(nodes, Duration.ofSeconds(2), "xyz");
        client = new Client(configuration);
        LOGGER.debug("Type sense Connection Successful ---> {}", client.health.retrieve().get("ok"));
    }

    private void checkInitialCollectionsExists(Client client) throws Exception {
        LOGGER.debug("Initializing task collection");
        makeInitialCollections(client);
    }

    public void makeInitialCollections(Client client) throws Exception {
        List<Field> fields = new ArrayList<>();
        fields.add(new Field().name("taskId").type(FieldTypes.STRING).facet(true).sort(true));
        fields.add(new Field().name("taskName").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("taskDescription").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("taskPriority").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("taskStatus").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("updatedCategory").type(FieldTypes.STRING));
        fields.add(new Field().name("initialCategory").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("parentTaskId").type(FieldTypes.STRING).facet(true).sort(true));
        fields.add(new Field().name("assignee").type(FieldTypes.STRING));
        fields.add(new Field().name("userId").type(FieldTypes.STRING));
        fields.add(new Field().name("taskStartDate").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("taskDueDate").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("taskCompleteDate").type(FieldTypes.STRING).optional(true));
        fields.add(new Field().name("createdDate").type(FieldTypes.STRING));
        fields.add(new Field().name("updatedDate").type(FieldTypes.STRING));
        CollectionSchema collectionSchema = new CollectionSchema();
        collectionSchema.name("task").fields(fields).defaultSortingField("taskId");

        client.collections().create(collectionSchema);
        LOGGER.debug("Initial collection created --> {}", client.collections("task").toString());
    }

    public Client getTypeSenseClient() throws Exception {
        if (client != null) {
            return client;
        } else {
            configure(environment.getProperty("typesense.host"), environment.getProperty("typesense.port"));
        }
        return client;
    }


}
