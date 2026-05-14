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
package org.openhab.io.mcp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the MCP server.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpConfiguration {

    public boolean enabled = true;
    public boolean exposeUntaggedItems = false;
    public int maxItemsPerPage = 100;
    /** Minimum ms between {@code resources/updated} notifications per resource URI. 0 disables coalescing. */
    public int resourceCoalesceMs = 500;
    /**
     * When true, exposes list/describe/call API meta-tools that let the agent invoke
     * any openHAB REST endpoint (including destructive ones). Off by default.
     */
    public boolean enableFullApiAccess = false;
    /**
     * When true, registers the {@code /mcp} path with the openHAB Cloud WebhookService
     * so remote MCP clients can reach this server via a stable myopenhab.org URL.
     * Requires the openhabcloud add-on to be installed and connected.
     */
    public boolean registerCloudWebhook = false;
    /**
     * Read-only field populated with the public webhook URL once {@link #registerCloudWebhook}
     * is enabled and the Cloud service has returned a hook registration. Shown in the
     * addon config UI so the user can copy it into their remote MCP client.
     */
    public String webhookUrl = "";
}
