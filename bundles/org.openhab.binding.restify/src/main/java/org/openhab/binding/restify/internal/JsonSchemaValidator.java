package org.openhab.binding.restify.internal;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.regex.JoniRegularExpressionFactory;

@Component
public class JsonSchemaValidator implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ENDPOINT_SCHEMA = "/endpoint-schema.json";
    private static final String GLOBAL_CONFIG_SCHEMA = "/global-config-schema.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<com.networknt.schema.Error> validateEndpointConfig(String config) {
        return validate(config, ENDPOINT_SCHEMA);
    }

    public List<com.networknt.schema.Error> validateGlobalConfig(String config) {
        return validate(config, GLOBAL_CONFIG_SCHEMA);
    }

    private List<com.networknt.schema.Error> validate(String config, String schemaPath) {
        var schemaRegistryConfig = SchemaRegistryConfig.builder()
                .regularExpressionFactory(JoniRegularExpressionFactory.getInstance()).build();
        var schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(schemaRegistryConfig)
                        /*
                         * This creates a mapping from $id which starts with
                         * https://www.example.org/schema to the retrieval IRI classpath:schema.
                         */
                        .schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers
                                .mapPrefix("https://www.openhab.org/addons/RESTify", "classpath:schema")));
        var schema = schemaRegistry
                .getSchema(SchemaLocation.of("https://www.openhab.org/addons/RESTify/schema" + schemaPath));
        return schema.validate(config, InputFormat.JSON, executionContext -> {
            /*
             * By default since Draft 2019-09 the format keyword only generates annotations
             * and not assertions.
             */
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
    }
}
