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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.auth.UserRegistry;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.io.mcp.internal.auth.McpAuthenticator;
import org.openhab.io.mcp.internal.servlet.JavaxStreamableServerTransportProvider;
import org.openhab.io.mcp.internal.servlet.OAuthMetadataServlet;
import org.openhab.io.mcp.internal.servlet.OAuthRegisterServlet;
import org.openhab.io.mcp.internal.servlet.OAuthTokenProxyServlet;
import org.openhab.io.mcp.internal.tools.ApiTools;
import org.openhab.io.mcp.internal.tools.ItemTools;
import org.openhab.io.mcp.internal.tools.LinkTools;
import org.openhab.io.mcp.internal.tools.ResourceProvider;
import org.openhab.io.mcp.internal.tools.RuleTools;
import org.openhab.io.mcp.internal.tools.SemanticTools;
import org.openhab.io.mcp.internal.tools.SystemTools;
import org.openhab.io.mcp.internal.tools.ThingTools;
import org.openhab.io.mcp.internal.tools.WatchTools;
import org.openhab.io.mcp.internal.util.McpEventBridge;
import org.openhab.io.mcp.internal.util.SubscriptionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Main OSGi component for the MCP server addon. Registers an MCP Streamable-HTTP
 * server at {@code /mcp} that exposes openHAB's semantic model, items, things, and
 * rules to AI agents.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = McpService.class, configurationPid = "org.openhab.mcp", property = {
        Constants.SERVICE_PID + "=org.openhab.mcp" })
@ConfigurableService(category = "io", label = "Model Context Protocol (MCP)", description_uri = "io:mcp")
public class McpService {

    private static final String SERVLET_PATH = "/mcp";
    private static final String SERVER_NAME = "openhab-mcp";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String HTTPCLIENT_NAME = "mcp-api";
    private static final String LOCAL_BASE_URL_DEFAULT = "http://localhost:8080";

    private final Logger logger = LoggerFactory.getLogger(McpService.class);

    private final ItemRegistry itemRegistry;
    private final ItemBuilderFactory itemBuilderFactory;
    private final MetadataRegistry metadataRegistry;
    private final UserRegistry userRegistry;
    private final EventPublisher eventPublisher;
    private final ThingRegistry thingRegistry;
    private final RuleRegistry ruleRegistry;
    private final RuleManager ruleManager;
    private final ItemChannelLinkRegistry linkRegistry;
    private final HttpClient httpClient;
    private final BundleContext bundleContext;
    private final ConfigurationAdmin configAdmin;

    private static final String MCP_ONESHOT_TAG = "MCP-oneshot";
    private static final String MCP_FIRE_AT_KEY = "mcpFireAt";
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    private @Nullable McpSyncServer mcpServer;
    private final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();
    private @Nullable ScheduledFuture<?> cleanupTask;
    private @Nullable McpCloudWebhookService cloudWebhook;
    private volatile McpConfiguration config = new McpConfiguration();

    @Activate
    public McpService(@Reference ItemRegistry itemRegistry, @Reference ItemBuilderFactory itemBuilderFactory,
            @Reference MetadataRegistry metadataRegistry, @Reference UserRegistry userRegistry,
            @Reference EventPublisher eventPublisher, @Reference ThingRegistry thingRegistry,
            @Reference RuleRegistry ruleRegistry, @Reference RuleManager ruleManager,
            @Reference ItemChannelLinkRegistry linkRegistry, @Reference HttpClientFactory httpClientFactory,
            @Reference ConfigurationAdmin configAdmin, BundleContext bundleContext, Map<String, ?> properties) {
        this.itemRegistry = itemRegistry;
        this.itemBuilderFactory = itemBuilderFactory;
        this.metadataRegistry = metadataRegistry;
        this.userRegistry = userRegistry;
        this.eventPublisher = eventPublisher;
        this.thingRegistry = thingRegistry;
        this.ruleRegistry = ruleRegistry;
        this.ruleManager = ruleManager;
        this.linkRegistry = linkRegistry;
        this.httpClient = httpClientFactory.createHttpClient(HTTPCLIENT_NAME);
        this.bundleContext = bundleContext;
        this.configAdmin = configAdmin;

        try {
            this.httpClient.start();
        } catch (Exception e) {
            logger.warn("Failed to start MCP HTTP client: {}", e.getMessage(), e);
            return;
        }

        this.config = buildConfig(properties);
        startServer();
    }

    @Modified
    protected void modified(Map<String, ?> properties) {
        McpConfiguration prev = this.config;
        McpConfiguration next = buildConfig(properties);
        this.config = next;
        // A config update that only differs in webhookUrl is almost always our own
        // write-back after registering the cloud hook — skip the server restart to
        // avoid a feedback loop.
        if (!nonUrlFieldsChanged(prev, next)) {
            return;
        }
        stopServer();
        // If the user just turned the cloud webhook feature off, proactively remove
        // the hook from the Cloud. Normal service/bundle lifecycle restarts do NOT
        // hit this path, so the registered UUID stays stable across deploys.
        if (prev.registerCloudWebhook && !next.registerCloudWebhook) {
            new McpCloudWebhookService(bundleContext, SERVLET_PATH, httpClient).unregister();
            persistWebhookUrl("");
        }
        startServer();
    }

    private static boolean nonUrlFieldsChanged(McpConfiguration a, McpConfiguration b) {
        return a.enabled != b.enabled || a.exposeUntaggedItems != b.exposeUntaggedItems
                || a.maxItemsPerPage != b.maxItemsPerPage || a.resourceCoalesceMs != b.resourceCoalesceMs
                || a.enableFullApiAccess != b.enableFullApiAccess || a.registerCloudWebhook != b.registerCloudWebhook;
    }

    @Deactivate
    protected void deactivate() {
        stopServer();
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.trace("Error stopping MCP HTTP client: {}", e.getMessage());
        }
    }

    private void startServer() {
        if (!config.enabled) {
            logger.debug("MCP server is disabled");
            return;
        }

        try {
            McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(new ObjectMapper());

            ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("mcp");

            McpCloudWebhookService webhook = null;
            if (config.registerCloudWebhook) {
                webhook = new McpCloudWebhookService(bundleContext, SERVLET_PATH, httpClient);
                this.cloudWebhook = webhook;
                McpCloudWebhookService webhookRef = webhook;
                scheduler.execute(() -> {
                    String mcpHook = webhookRef.register() ? webhookRef.getPublicUrl() : null;
                    if (mcpHook != null) {
                        logger.debug("MCP cloud webhook URL: {}", mcpHook);
                    }
                    persistWebhookUrl(mcpHook != null ? mcpHook : "");
                });
            } else {
                persistWebhookUrl("");
            }

            String localBaseUrl = resolveLocalBaseUrl();
            McpAuthenticator authenticator = new McpAuthenticator(userRegistry, httpClient, localBaseUrl);

            JavaxStreamableServerTransportProvider transport = new JavaxStreamableServerTransportProvider(jsonMapper,
                    authenticator, webhook);

            // Register as OSGi HTTP Whiteboard servlet (exact /mcp — Streamable HTTP's
            // single endpoint; the servlet handles GET, POST, DELETE on this path).
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, SERVLET_PATH);
            props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED, true);
            props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "mcp-streamable");
            serviceRegistrations.add(bundleContext.registerService(Servlet.class, transport, props));

            OAuthMetadataServlet metadataServlet = new OAuthMetadataServlet(webhook);
            serviceRegistrations.add(
                    registerServlet(metadataServlet, OAuthMetadataServlet.PATH_PROTECTED_RESOURCE, "mcp-oauth-prm"));
            serviceRegistrations
                    .add(registerServlet(metadataServlet, OAuthMetadataServlet.PATH_AUTH_SERVER, "mcp-oauth-asmd"));
            serviceRegistrations.add(registerServlet(metadataServlet, OAuthMetadataServlet.PATH_AUTH_SERVER_OIDC,
                    "mcp-oauth-asmd-oidc"));

            OAuthTokenProxyServlet tokenProxy = new OAuthTokenProxyServlet(httpClient, localBaseUrl);
            serviceRegistrations.add(registerServlet(tokenProxy, OAuthTokenProxyServlet.PATH, "mcp-oauth-token-proxy"));

            OAuthRegisterServlet registerServlet = new OAuthRegisterServlet();
            serviceRegistrations.add(registerServlet(registerServlet, OAuthRegisterServlet.PATH, "mcp-oauth-register"));

            SubscriptionManager subscriptions = new SubscriptionManager();
            transport.setSubscriptionManager(subscriptions);

            ItemTools itemTools = new ItemTools(itemRegistry, itemBuilderFactory, metadataRegistry, eventPublisher,
                    jsonMapper);
            SemanticTools semanticTools = new SemanticTools(itemRegistry, metadataRegistry, jsonMapper,
                    config.exposeUntaggedItems);
            ThingTools thingTools = new ThingTools(thingRegistry, linkRegistry, jsonMapper);
            RuleTools ruleTools = new RuleTools(ruleRegistry, ruleManager, jsonMapper);
            LinkTools linkTools = new LinkTools(linkRegistry, itemRegistry, thingRegistry, jsonMapper);
            SystemTools systemTools = new SystemTools(itemRegistry, thingRegistry, ruleRegistry, jsonMapper);
            ResourceProvider resourceProvider = new ResourceProvider(itemRegistry, metadataRegistry, thingRegistry,
                    linkRegistry, ruleRegistry, ruleManager, jsonMapper, config.exposeUntaggedItems);
            WatchTools watchTools = new WatchTools(subscriptions, jsonMapper);

            McpSchema.ServerCapabilities capabilities = McpSchema.ServerCapabilities.builder().tools(true)
                    .resources(true, true).build();

            var builder = McpServer.sync(transport).jsonMapper(jsonMapper)
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator()).serverInfo(SERVER_NAME, SERVER_VERSION)
                    .capabilities(capabilities).resources(resourceProvider.resources())
                    .resourceTemplates(resourceProvider.templates()).instructions(buildInstructions())
                    .toolCall(semanticTools.getSemanticModelTool(),
                            (exchange, req) -> semanticTools.handleGetSemanticModel(req))
                    .toolCall(itemTools.getSearchItemsTool(), (exchange, req) -> itemTools.handleSearchItems(req))
                    .toolCall(itemTools.getItemTool(), (exchange, req) -> itemTools.handleGetItem(req))
                    .toolCall(itemTools.getCreateItemTool(), (exchange, req) -> itemTools.handleCreateItem(req))
                    .toolCall(itemTools.getUpdateItemTool(), (exchange, req) -> itemTools.handleUpdateItem(req))
                    .toolCall(itemTools.getDeleteItemTool(), (exchange, req) -> itemTools.handleDeleteItem(req))
                    .toolCall(itemTools.getSendCommandTool(), (exchange, req) -> itemTools.handleSendCommand(req))
                    .toolCall(itemTools.getUpdateStateTool(), (exchange, req) -> itemTools.handleUpdateState(req))
                    .toolCall(thingTools.getThingsTool(), (exchange, req) -> thingTools.handleGetThings(req))
                    .toolCall(thingTools.getThingDetailsTool(),
                            (exchange, req) -> thingTools.handleGetThingDetails(req))
                    .toolCall(ruleTools.getRulesTool(), (exchange, req) -> ruleTools.handleGetRules(req))
                    .toolCall(ruleTools.getCreateRuleTool(), (exchange, req) -> ruleTools.handleCreateRule(req))
                    .toolCall(ruleTools.getUpdateRuleTool(), (exchange, req) -> ruleTools.handleUpdateRule(req))
                    .toolCall(ruleTools.getManageRuleTool(), (exchange, req) -> ruleTools.handleManageRule(req))
                    .toolCall(linkTools.getLinksTool(), (exchange, req) -> linkTools.handleGetLinks(req))
                    .toolCall(linkTools.getCreateLinkTool(), (exchange, req) -> linkTools.handleCreateLink(req))
                    .toolCall(linkTools.getDeleteLinkTool(), (exchange, req) -> linkTools.handleDeleteLink(req))
                    .toolCall(systemTools.getSystemInfoTool(), (exchange, req) -> systemTools.handleGetSystemInfo(req))
                    .toolCall(systemTools.getHomeStatusTool(), (exchange, req) -> systemTools.handleGetHomeStatus(req))
                    .toolCall(watchTools.getWatchItemsTool(),
                            (exchange, req) -> watchTools.handleWatchItems(exchange, req))
                    .toolCall(watchTools.getUnwatchItemsTool(),
                            (exchange, req) -> watchTools.handleUnwatchItems(exchange, req))
                    .toolCall(watchTools.getEventsTool(), (exchange, req) -> watchTools.handleGetEvents(exchange, req));

            if (config.enableFullApiAccess) {
                ApiTools apiTools = new ApiTools(httpClient, localBaseUrl, transport::getSessionToken, jsonMapper);
                builder = builder
                        .toolCall(apiTools.getListApiEndpointsTool(),
                                (exchange, req) -> apiTools.handleListApiEndpoints(exchange, req))
                        .toolCall(apiTools.getDescribeApiEndpointTool(),
                                (exchange, req) -> apiTools.handleDescribeApiEndpoint(exchange, req))
                        .toolCall(apiTools.getCallApiTool(), (exchange, req) -> apiTools.handleCallApi(exchange, req));
            }

            McpSyncServer server = builder.build();

            this.mcpServer = server;

            McpEventBridge eventBridge = new McpEventBridge(server, subscriptions, config.resourceCoalesceMs);
            serviceRegistrations.add(bundleContext.registerService(EventSubscriber.class, eventBridge, null));

            cleanupTask = scheduler.scheduleWithFixedDelay(this::cleanupOneshotRules, CLEANUP_INTERVAL_SECONDS,
                    CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);

            logger.debug("MCP server started at {}", SERVLET_PATH);
        } catch (Exception e) {
            logger.debug("Failed to start MCP server: {}", e.getMessage(), e);
        }
    }

    private void stopServer() {
        ScheduledFuture<?> task = cleanupTask;
        if (task != null) {
            task.cancel(false);
            cleanupTask = null;
        }

        // Don't call webhook.unregister() here on normal lifecycle — removing and
        // re-requesting the hook makes the Cloud issue a NEW UUID, which kills any
        // clients that saved the old URL. Instead we only stop the refresh task;
        // the Cloud's 30-day TTL will expire the hook if we never come back.
        McpCloudWebhookService webhook = cloudWebhook;
        if (webhook != null) {
            webhook.stopRefresh();
            cloudWebhook = null;
        }

        McpSyncServer server = mcpServer;
        if (server != null) {
            try {
                server.closeGracefully();
            } catch (Exception e) {
                logger.trace("Error during MCP server shutdown: {}", e.getMessage());
            }
            mcpServer = null;
        }

        for (ServiceRegistration<?> reg : serviceRegistrations) {
            try {
                reg.unregister();
            } catch (Exception e) {
                logger.trace("Error unregistering service: {}", e.getMessage());
            }
        }
        serviceRegistrations.clear();
    }

    private ServiceRegistration<Servlet> registerServlet(Servlet servlet, String pattern, String name) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, pattern);
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, name);
        return bundleContext.registerService(Servlet.class, servlet, props);
    }

    private String resolveLocalBaseUrl() {
        int port = HttpServiceUtil.getHttpServicePort(bundleContext);
        if (port > 0) {
            return "http://localhost:" + port;
        }
        return LOCAL_BASE_URL_DEFAULT;
    }

    private String buildInstructions() {
        String base = """
                This MCP server provides access to an openHAB smart home system.

                Start by calling get_semantic_model to understand the home layout \
                (rooms, equipment, controllable devices). Use search_items to find \
                specific items by name or type. Use get_item for details on specific \
                items. Use send_command to control devices (ON/OFF, dimmer levels, etc). \
                Item names are case-sensitive and must match exactly.

                Use create_item to add new items, update_item to modify labels/tags/groups, \
                and delete_item to remove items. Use get_links and create_link to wire items \
                to thing channels. Use delete_link to remove wiring.

                Use create_rule to set up automations. If the user says 'create a rule' \
                or implies a persistent automation, use recurring triggers (time_of_day, \
                cron, item_state_change). Only use 'datetime' (one-shot) when the user \
                explicitly wants a one-time action on a specific date (e.g. 'at 3pm today'). \
                Use update_rule to modify a rule's name, description, tags, or actions \
                without recreating it. \
                List rules you created with get_rules(tag='MCP'). Remove them with \
                manage_rule(action='remove').

                If the user cancels, reverses, or says 'never mind' about an automation \
                you just scheduled (e.g. 'don't turn the lights off at 7 after all'), call \
                manage_rule(action='remove') with the ruleUID from your earlier create_rule \
                response. Remember: you received the ruleUID when you created the rule — \
                use it to cancel. Never leave dangling one-shot rules when the user has \
                changed their mind. If you've lost track of the UID, call \
                get_rules(tag='MCP-oneshot') to find pending one-shots by name.

                To monitor items for changes, call watch_items with the item names you \
                care about. Then when the user asks 'did anything happen' or 'what \
                changed', call get_events to retrieve the buffered list of state changes \
                since your last check. Stop watching with unwatch_items. This is the \
                recommended way to handle 'let me know when X happens' requests. \
                (Advanced clients can also use resources/subscribe on 'openhab://item/<name>' \
                for push notifications.)
                """;
        if (!config.enableFullApiAccess) {
            return base;
        }
        return base + """

                For REST endpoints not covered by the curated tools above, use \
                list_api_endpoints, describe_api_endpoint, and call_api. These give \
                you access to the full openHAB REST API, including destructive \
                endpoints (delete items, modify things, change service configs). \
                Workflow: call list_api_endpoints to discover, describe_api_endpoint \
                to understand the schema, then call_api to invoke. Prefer the curated \
                tools when they cover the task — the meta-tools are the escape hatch.
                """;
    }

    private void cleanupOneshotRules() {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.systemDefault());
        for (Rule rule : ruleRegistry.getAll()) {
            if (!rule.getTags().contains(MCP_ONESHOT_TAG)) {
                continue;
            }
            Object fireAt = rule.getConfiguration().get(MCP_FIRE_AT_KEY);
            if (fireAt instanceof String s) {
                try {
                    LocalDateTime target = LocalDateTime.parse(s);
                    if (target.isBefore(now)) {
                        ruleRegistry.remove(rule.getUID());
                        logger.debug("Cleaned up fired one-shot MCP rule: {} ({})", rule.getUID(), rule.getName());
                    }
                } catch (Exception e) {
                    logger.trace("Could not parse fireAt for rule {}: {}", rule.getUID(), e.getMessage());
                }
            }
        }
    }

    private static McpConfiguration buildConfig(Map<String, ?> properties) {
        McpConfiguration cfg = new McpConfiguration();
        cfg.enabled = toBoolean(properties.get("enabled"), cfg.enabled);
        cfg.exposeUntaggedItems = toBoolean(properties.get("exposeUntaggedItems"), cfg.exposeUntaggedItems);
        cfg.maxItemsPerPage = toInt(properties.get("maxItemsPerPage"), cfg.maxItemsPerPage);
        cfg.resourceCoalesceMs = toInt(properties.get("resourceCoalesceMs"), cfg.resourceCoalesceMs);
        cfg.enableFullApiAccess = toBoolean(properties.get("enableFullApiAccess"), cfg.enableFullApiAccess);
        cfg.registerCloudWebhook = toBoolean(properties.get("registerCloudWebhook"), cfg.registerCloudWebhook);
        Object url = properties.get("webhookUrl");
        if (url instanceof String s) {
            cfg.webhookUrl = s;
        }
        return cfg;
    }

    /**
     * Writes the public webhook URL back into this component's config so the user can
     * see (and copy) it from the addon config UI. Triggers a {@link #modified} callback
     * that we filter out in {@link #nonUrlFieldsChanged}.
     */
    private void persistWebhookUrl(String url) {
        try {
            Configuration cfg = configAdmin.getConfiguration("org.openhab.mcp");
            Dictionary<String, Object> props = cfg.getProperties();
            if (props == null) {
                props = new Hashtable<>();
            }
            Object existing = props.get("webhookUrl");
            if (Objects.equals(existing != null ? existing.toString() : "", url)) {
                return;
            }
            props.put("webhookUrl", url);
            cfg.update(props);
        } catch (Exception e) {
            logger.debug("Could not persist webhookUrl config property: {}", e.getMessage());
        }
    }

    private static int toInt(@Nullable Object value, int defaultValue) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private static boolean toBoolean(@Nullable Object value, boolean defaultValue) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return defaultValue;
    }
}
