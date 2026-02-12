package org.openhab.binding.restify.internal.config;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

class JsonSchemaValidator implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String ENDPOINT_SCHEMA = "/schemas/endpoint-schema.json";
    private static final String GLOBAL_CONFIG_SCHEMA = "/schemas/global-config-schema.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    Set<ValidationMessage> validateEndpointConfig(String config) {
        return validate(config, ENDPOINT_SCHEMA);
    }

    Set<ValidationMessage> validateGlobalConfig(String config) {
        return validate(config, GLOBAL_CONFIG_SCHEMA);
    }

    private Set<ValidationMessage> validate(String config, String schemaPath) {
        var schemaStream = JsonSchemaValidator.class.getResourceAsStream(schemaPath);
        var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        var schema = factory.getSchema(schemaStream);
        try {
            var json = mapper.readTree(config);
            return schema.validate(json);
        } catch (JsonProcessingException e) {
            return Set.of(ValidationMessage.builder().type("json.parse.error").message(e.getOriginalMessage()).build());
        }
    }
}
