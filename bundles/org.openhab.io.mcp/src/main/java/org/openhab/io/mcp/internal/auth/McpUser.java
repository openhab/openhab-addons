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
package org.openhab.io.mcp.internal.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.modelcontextprotocol.server.McpSyncServerExchange;

/**
 * The user behind an MCP request.
 *
 * @param name username, empty for JWTs
 * @param admin true for an administrator
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record McpUser(String name, boolean admin) {

    /** Key for the request's user in the MCP transport context. */
    public static final String CONTEXT_KEY = "openhab.mcp.user";

    /**
     * @return the user of the request being handled, or {@code null} if none was set
     */
    public static @Nullable McpUser of(McpSyncServerExchange exchange) {
        return exchange.transportContext().get(CONTEXT_KEY) instanceof McpUser user ? user : null;
    }

    public static boolean isAdmin(McpSyncServerExchange exchange) {
        McpUser user = of(exchange);
        return user != null && user.admin();
    }
}
