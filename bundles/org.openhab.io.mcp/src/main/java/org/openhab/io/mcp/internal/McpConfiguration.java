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
     * When true, enables JavaScript scripting features for the agent: script-typed
     * rule actions and conditions, plus the {@code execute_script} tool that runs a
     * JS snippet ad-hoc. Scripts have full system access (HTTP, Exec, OSGi, every
     * item) so this is off by default and should only be enabled when the operator
     * trusts the connected MCP clients. Requires the {@code openhab-automation-jsscripting}
     * add-on to be installed for scripts to actually execute.
     */
    public boolean enableScripting = false;
    /**
     * When true, exposes diagnostic logging tools ({@code get_logs} and {@code manage_log_level}
     * with actions {@code get}/{@code set}) so an agent can read recent log entries and adjust
     * logger verbosity to troubleshoot bindings and rules. Level changes are sent through the
     * openHAB REST API and require the caller's bearer token to have ADMIN scope; reads are
     * gated by this flag alone. Off by default.
     */
    public boolean enableLoggingAccess = false;
    /**
     * When true, exposes Main UI design tools ({@code list_widgets}, {@code describe_widget},
     * {@code get_page_skeleton}, {@code manage_ui_component}, {@code validate_ui_component})
     * so an agent can create, modify, and delete Main UI pages and custom widgets. The tools
     * embed a curated widget catalog and expression/action reference so the agent can author
     * pages without hallucinating component names or prop keys. CRUD writes go through the
     * openHAB REST API at {@code /rest/ui/components/} and require the caller's bearer token
     * to have ADMIN scope; reads are gated by this flag alone. Off by default.
     */
    public boolean enableUiDesign = false;
    /**
     * When true, exposes {@code manage_static_asset} so an agent can list, read, write, and
     * delete files in {@code $OPENHAB_CONF/html} (the folder served at {@code /static/*} and
     * used for plan-page backgrounds, custom widget icons, and CSS overrides). The tool writes
     * directly to disk (there is no REST gate to forward to), so the bundle enforces
     * administrator scope itself by probing an admin-gated REST endpoint with the session's
     * bearer token before each call (HTTP 200 means admin). Filenames are sanitized against
     * path traversal (including symlinks pointing outside the root), extensions are whitelisted,
     * and per-call uploads are capped at 10 MB. Off by default.
     */
    public boolean enableStaticAssets = false;
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
