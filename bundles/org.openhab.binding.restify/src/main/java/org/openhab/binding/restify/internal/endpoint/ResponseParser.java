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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.restify.internal.servlet.Response;
import org.osgi.service.component.annotations.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = ResponseParser.class, immediate = true)
public class ResponseParser {

    public Response parseResponse(JsonNode response) throws EndpointParseException {
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
        if (response.isNumber()) {
            return new Response.NumberResponse(response.numberValue());
        }
        if (response.isBoolean()) {
            return new Response.BooleanResponse(response.asBoolean());
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
            schemaMap.put(requireNonNull(entry.getKey()), parseResponse(entry.getValue()));
        }
        return new Response.JsonResponse(Map.copyOf(schemaMap));
    }

    private Response.ArrayResponse parseFromArray(ArrayNode arrayNode) throws EndpointParseException {
        var schemas = new ArrayList<Response>(arrayNode.size());
        for (var node : arrayNode) {
            schemas.add(parseResponse(node));
        }
        return new Response.ArrayResponse(List.copyOf(schemas));
    }
}
