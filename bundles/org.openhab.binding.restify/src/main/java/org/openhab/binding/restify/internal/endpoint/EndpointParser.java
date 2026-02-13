/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.restify.internal.endpoint;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.openhab.binding.restify.internal.servlet.Response;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class EndpointParser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    public Endpoint parseEndpointConfig(String config) throws EndpointParseException {
        return parseResponse(config);
    }

    private Map<String, String> parseUsernamePasswords(@Nullable JsonNode authentication)
            throws EndpointParseException {
        if (authentication == null || authentication.isNull()) {
            return Map.of();
        }
        if (!authentication.isObject()) {
            throw new EndpointParseException("Authentication should be a JSON object!");
        }
        var usernamePasswords = authentication.get("usernamePasswords");
        if (usernamePasswords == null || usernamePasswords.isNull()) {
            return Map.of();
        }
        if (!(usernamePasswords instanceof ObjectNode usernamePasswordsObject)) {
            throw new EndpointParseException("usernamePasswords should be a JSON object!");
        }
        var result = new HashMap<String, String>();
        for (var entry : usernamePasswordsObject.properties()) {
            result.put(entry.getKey(), entry.getValue().asString());
        }
        return Map.copyOf(result);
    }

    private Endpoint parseResponse(String json) throws EndpointParseException {
        try {
            var root = mapper.readTree(json);
            var authorizationNode = root.get("authorization");
            @Nullable
            Authorization authorization = authorizationNode != null && !authorizationNode.isNull()
                    ? parseAuthorization(authorizationNode)
                    : null;
            var responseNode = root.get("response");
            if (responseNode == null || responseNode.isNull() || !responseNode.isObject()) {
                throw new EndpointParseException("Response should be a JSON object!");
            }
            var schema = parseFromObject((ObjectNode) responseNode);
            return new Endpoint(authorization, schema);
        } catch (JacksonException e) {
            throw new EndpointParseException("Cannot read tree from: " + json, e);
        }
    }

    private Authorization parseAuthorization(JsonNode authorization) throws EndpointParseException {
        if (!authorization.isObject()) {
            throw new EndpointParseException("Authorization should be a JSON object!");
        }
        var type = getText(authorization, "type");
        if ("Basic".equalsIgnoreCase(type)) {
            return new Authorization.Basic(getText(authorization, "username"), getText(authorization, "password"));
        }
        if ("Bearer".equalsIgnoreCase(type)) {
            return new Authorization.Bearer(getText(authorization, "token"));
        }
        throw new EndpointParseException("Unknown authorization type: " + type);
    }

    private Response parseSchema(JsonNode response) throws EndpointParseException {
        if (response.isNull()) {
            throw new EndpointParseException("Response schema cannot be null!");
        }
        if (response.isObject()) {
            return parseFromObject((ObjectNode) response);
        }
        if (response.isArray()) {
            return parseFromArray((ArrayNode) response);
        }
        if (response.isString()) {
            return parseFromString(response.asString());
        }
        throw new EndpointParseException("Unsupported schema type: " + response.getNodeType());
    }

    private Response parseFromString(String string) throws EndpointParseException {
        if (!string.startsWith("$")) {
            return new Response.StringResponse(string);
        }
        if (string.startsWith("$item.")) {
            var uuidExpression = findUuidExpression(string, "$item.");
            return new Response.ItemResponse(uuidExpression.uuid, uuidExpression.expression);
        }
        if (string.startsWith("$thing.")) {
            var uuidExpression = findUuidExpression(string, "$thing.");
            return new Response.ThingResponse(uuidExpression.uuid, uuidExpression.expression);
        }
        throw new EndpointParseException("Unsupported schema type: " + string);
    }

    private UuidExpression findUuidExpression(String string, String prefix) {
        var withoutPrefix = string.substring(prefix.length());
        String uuid;
        var dotIndex = withoutPrefix.indexOf(".");
        if (dotIndex > -1) {
            uuid = withoutPrefix.substring(0, dotIndex);
        } else {
            uuid = withoutPrefix;
        }
        var expression = withoutPrefix.substring(dotIndex + 1);
        return new UuidExpression(uuid, expression);
    }

    private record UuidExpression(String uuid, String expression) {
    }

    private Response.JsonResponse parseFromObject(ObjectNode objectNode) throws EndpointParseException {
        var schemaMap = new HashMap<String, Response>();
        for (var entry : objectNode.properties()) {
            schemaMap.put(requireNonNull(entry.getKey()), parseSchema(entry.getValue()));
        }
        return new Response.JsonResponse(Map.copyOf(schemaMap));
    }

    private Response.ArrayResponse parseFromArray(ArrayNode arrayNode) throws EndpointParseException {
        var schemas = new ArrayList<Response>(arrayNode.size());
        for (var node : arrayNode) {
            schemas.add(parseSchema(node));
        }
        return new Response.ArrayResponse(List.copyOf(schemas));
    }

    private String getText(JsonNode node, String fieldName) throws EndpointParseException {
        var value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new EndpointParseException("Missing required field: " + fieldName);
        }
        return value.asString();
    }

    private int getInt(JsonNode node, String fieldName) throws EndpointParseException {
        var value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new EndpointParseException("Missing required field: " + fieldName);
        }
        return value.asInt();
    }
}
