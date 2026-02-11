package org.openhab.binding.restify.internal.config;

import static org.openhab.binding.restify.internal.config.GlobalConfig.EMPTY;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.Authorization;
import org.openhab.binding.restify.internal.RequestProcessor.Method;
import org.openhab.binding.restify.internal.Response;
import org.openhab.binding.restify.internal.Schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public Config parse(ConfigContent config) {
        return new Config(config.globalConfig().map(this::parseGlobalConfig).orElse(EMPTY),
                config.endpoints().stream().map(this::parseResponse).toList());
    }

    private GlobalConfig parseGlobalConfig(String content) {
        try {
            var map = mapper.readValue(content, new TypeReference<Map<String, Object>>() {
            });
            var version = (String) map.get("version");
            var usernamePasswords = parseUsernamePasswords(map.get("authentication"));
            return new GlobalConfig(version, usernamePasswords);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse %s from JSON! Schema should be validated earlier and this should not happen! json=%s"
                            .formatted(GlobalConfig.class.getSimpleName(), content),
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseUsernamePasswords(@Nullable Object authentication) {
        try {
            if (authentication == null) {
                return Map.of();
            }
            var map = (Map<String, Object>) authentication;
            var usernamePasswords = map.get("usernamePasswords");
            if (usernamePasswords == null) {
                return Map.of();
            }
            return ((Map<String, String>) usernamePasswords);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse username/password from JSON! Schema should be validated earlier and this should not happen! json=%s"
                            .formatted(authentication),
                    e);
        }
    }

    private Response parseResponse(String json) {
        try {
            var map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            var path = (String) map.get("path");
            var method = Method.valueOf((String) map.get("method"));
            @Nullable
            Authorization authorization = map.containsKey("authorization")
                    ? parseAuthorization(map.get("authorization"))
                    : null;
            var schema = parseSchema(map.get("response"));
            return new Response(path, method, authorization, schema);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse %s from JSON! Schema should be validated earlier and this should not happen! json=%s"
                            .formatted(Response.class.getSimpleName(), json),
                    e);
        }
    }

    private Authorization parseAuthorization(Object authorization) {
        try {
            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) authorization;
            var type = (String) map.get("type");
            return switch (type) {
                case "Basic" -> new Authorization.Basic((String) map.get("username"), (String) map.get("password"));
                case "Bearer" -> new Authorization.Bearer((String) map.get("token"));
                default -> throw new IllegalArgumentException("Unknown authorization type: " + type);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse %s from JSON! Schema should be validated earlier and this should not happen! json=%s"
                            .formatted(Authorization.class.getSimpleName(), authorization),
                    e);
        }
    }

    private Schema parseSchema(Object response) {
        return switch (response) {
            case Map<?, ?> map -> parseFromMap(map);
            case List<?> list -> parseFromList(list);
            case String s -> parseFromString(s);
            default -> throw new IllegalArgumentException("Unsupported schema type: " + response.getClass());
        };
    }

    private Schema parseFromString(String string) {
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
        throw new IllegalArgumentException("Unsupported schema type: " + string);
    }

    private UuidExpression findUuidExpression(String string, String prefix) {
        var withoutPrefix = string.substring(prefix.length());
        var dotIndex = withoutPrefix.indexOf(".");
        if (dotIndex == -1) {
            throw new IllegalArgumentException("Invalid schema expression: " + string);
        }
        var uuid = withoutPrefix.substring(0, dotIndex);
        var expression = withoutPrefix.substring(dotIndex + 1);
        return new UuidExpression(uuid, expression);
    }

    private record UuidExpression(String uuid, String expression) {
    }

    private Schema parseFromMap(Map<?, ?> map) {
        var schemaMap = map.entrySet().stream()
                .map(entry -> Map.entry((String) entry.getKey(), parseSchema(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Schema.JsonSchema(schemaMap);
    }

    private Schema parseFromList(List<?> list) {
        return new Schema.ArraySchema(list.stream().map(this::parseSchema).toList());
    }
}
