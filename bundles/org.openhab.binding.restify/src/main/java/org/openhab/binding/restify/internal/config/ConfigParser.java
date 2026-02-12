package org.openhab.binding.restify.internal.config;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.openhab.binding.restify.internal.servlet.Response;
import org.openhab.binding.restify.internal.servlet.Schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConfigParser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    public Response parseEndpointConfig(String config) throws ConfigException {
        return parseResponse(config);
    }

    public Config parseConfig(String content) throws ConfigException {
        try {
            var root = mapper.readTree(content);
            var version = getInt(root, "version");
            var usernamePasswords = parseUsernamePasswords(root.get("authentication"));
            return new Config(version, usernamePasswords);
        } catch (JsonProcessingException e) {
            throw new ConfigException("Cannot read tree from: " + content, e);
        }
    }

    private Map<String, String> parseUsernamePasswords(@Nullable JsonNode authentication) throws ConfigException {
        if (authentication == null || authentication.isNull()) {
            return Map.of();
        }
        if (!authentication.isObject()) {
            throw new ConfigException("Authentication should be a JSON object!");
        }
        var usernamePasswords = authentication.get("usernamePasswords");
        if (usernamePasswords == null || usernamePasswords.isNull()) {
            return Map.of();
        }
        if (!(usernamePasswords instanceof ObjectNode usernamePasswordsObject)) {
            throw new ConfigException("usernamePasswords should be a JSON object!");
        }
        var result = new HashMap<String, String>();
        for (var entry : usernamePasswordsObject.properties()) {
            result.put(entry.getKey(), entry.getValue().asText());
        }
        return Map.copyOf(result);
    }

    private Response parseResponse(String json) throws ConfigException {
        try {
            var root = mapper.readTree(json);
            var authorizationNode = root.get("authorization");
            @Nullable
            Authorization authorization = authorizationNode != null && !authorizationNode.isNull()
                    ? parseAuthorization(authorizationNode)
                    : null;
            var responseNode = root.get("response");
            if (responseNode == null || responseNode.isNull() || !responseNode.isObject()) {
                throw new ConfigException("Response should be a JSON object!");
            }
            var schema = parseFromObject((ObjectNode) responseNode);
            return new Response(authorization, schema);
        } catch (JsonProcessingException e) {
            throw new ConfigException("Cannot read tree from: " + json, e);
        }
    }

    private Authorization parseAuthorization(JsonNode authorization) throws ConfigException {

        if (!authorization.isObject()) {
            throw new ConfigException("Authorization should be a JSON object!");
        }
        var type = getText(authorization, "type");
        return switch (type) {
            case "Basic" -> new Authorization.Basic(getText(authorization, "username"));
            case "Bearer" -> new Authorization.Bearer(getText(authorization, "token"));
            default -> throw new ConfigException("Unknown authorization type: " + type);
        };
    }

    private Schema parseSchema(JsonNode response) throws ConfigException {
        if (response == null || response.isNull()) {
            throw new ConfigException("Response schema cannot be null!");
        }
        if (response.isObject()) {
            return parseFromObject((ObjectNode) response);
        }
        if (response.isArray()) {
            return parseFromArray((ArrayNode) response);
        }
        if (response.isTextual()) {
            return parseFromString(response.asText());
        }
        throw new ConfigException("Unsupported schema type: " + response.getNodeType());
    }

    private Schema parseFromString(String string) throws ConfigException {
        if (!string.startsWith("$")) {
            return new Schema.StringSchema(string);
        }
        if (string.startsWith("$item.")) {
            var uuidExpression = findUuidExpression(string, "$item.");
            return new Schema.ItemSchema(uuidExpression.uuid, uuidExpression.expression);
        }
        if (string.startsWith("$thing.")) {
            var uuidExpression = findUuidExpression(string, "$item.");
            return new Schema.ThingSchema(uuidExpression.uuid, uuidExpression.expression);
        }
        throw new ConfigException("Unsupported schema type: " + string);
    }

    private UuidExpression findUuidExpression(String string, String prefix) throws ConfigException {
        var withoutPrefix = string.substring(prefix.length());
        var dotIndex = withoutPrefix.indexOf(".");
        if (dotIndex == -1) {
            throw new ConfigException("Invalid schema expression: " + string);
        }
        var uuid = withoutPrefix.substring(0, dotIndex);
        var expression = withoutPrefix.substring(dotIndex + 1);
        return new UuidExpression(uuid, expression);
    }

    private record UuidExpression(String uuid, String expression) {
    }

    private Schema.JsonSchema parseFromObject(ObjectNode objectNode) throws ConfigException {
        var schemaMap = new HashMap<String, Schema>();
        for (var entry : objectNode.properties()) {
            schemaMap.put(requireNonNull(entry.getKey()), parseSchema(entry.getValue()));
        }
        return new Schema.JsonSchema(Map.copyOf(schemaMap));
    }

    private Schema.ArraySchema parseFromArray(ArrayNode arrayNode) throws ConfigException {
        var schemas = new ArrayList<Schema>(arrayNode.size());
        for (var node : arrayNode) {
            schemas.add(parseSchema(node));
        }
        return new Schema.ArraySchema(List.copyOf(schemas));
    }

    private String getText(JsonNode node, String fieldName) throws ConfigException {
        var value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new ConfigException("Missing required field: " + fieldName);
        }
        return value.asText();
    }

    private int getInt(JsonNode node, String fieldName) throws ConfigException {
        var value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new ConfigException("Missing required field: " + fieldName);
        }
        return value.asInt();
    }
}
