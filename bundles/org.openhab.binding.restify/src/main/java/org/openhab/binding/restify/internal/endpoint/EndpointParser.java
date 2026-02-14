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

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.openhab.binding.restify.internal.servlet.Response;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = EndpointParser.class, immediate = true)
public class EndpointParser {
    private static final Pattern SECRET_VALUE_PATTERN = Pattern.compile(
            "(\"(?:password|token|defaultBasic|defaultBearer)\"\\s*:\\s*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(EndpointParser.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AuthorizationParser authorizationParser;
    private final ResponseParser responseParser;

    @Activate
    public EndpointParser(@Reference AuthorizationParser authorizationParser,
            @Reference ResponseParser responseParser) {
        this.authorizationParser = authorizationParser;
        this.responseParser = responseParser;
    }

    public Endpoint parseEndpoint(String json) throws EndpointParseException {
        try {
            var root = mapper.readTree(json);
            var authorizationNode = root.get("authorization");
            @Nullable
            Authorization authorization = authorizationNode != null && !authorizationNode.isNull()
                    ? authorizationParser.parseAuthorization(authorizationNode)
                    : null;
            var responseNode = root.get("response");
            if (responseNode == null || responseNode.isNull() || !responseNode.isObject()) {
                throw new EndpointParseException("Response should be a JSON object!");
            }
            var schema = responseParser.parseResponse((ObjectNode) responseNode);
            if (!(schema instanceof Response.JsonResponse jsonResponse)) {
                throw new EndpointParseException("Response should be a JSON object!");
            }
            return new Endpoint(authorization, jsonResponse);
        } catch (JacksonException e) {
            logger.debug("Cannot parse endpoint JSON: {}", redactSecrets(json), e);
            throw new EndpointParseException("Invalid endpoint JSON", e);
        }
    }

    private static String redactSecrets(String json) {
        return SECRET_VALUE_PATTERN.matcher(json).replaceAll("$1***$3");
    }
}
