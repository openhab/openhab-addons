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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.restify.internal.servlet.Authorization;
import org.osgi.service.component.annotations.Component;

import tools.jackson.databind.JsonNode;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(service = AuthorizationParser.class, immediate = true)
public class AuthorizationParser {

    public Authorization parseAuthorization(JsonNode authorization) throws EndpointParseException {
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

    private String getText(JsonNode node, String fieldName) throws EndpointParseException {
        var value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new EndpointParseException("Missing required field: " + fieldName);
        }
        return value.asString();
    }
}
