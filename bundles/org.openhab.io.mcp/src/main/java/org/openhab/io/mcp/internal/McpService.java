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

import java.nio.file.Path;
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
import org.openhab.core.OpenHAB;
import org.openhab.core.auth.UserRegistry;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.module.script.ScriptEngineManager;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.rest.WebhookService;
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
import org.openhab.io.mcp.internal.tools.LoggingTools;
import org.openhab.io.mcp.internal.tools.ResourceProvider;
import org.openhab.io.mcp.internal.tools.RuleTools;
import org.openhab.io.mcp.internal.tools.ScriptTools;
import org.openhab.io.mcp.internal.tools.SemanticTools;
import org.openhab.io.mcp.internal.tools.StaticAssetTools;
import org.openhab.io.mcp.internal.tools.SystemTools;
import org.openhab.io.mcp.internal.tools.ThingTools;
import org.openhab.io.mcp.internal.tools.UiTools;
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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.log.LogReaderService;
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
    private final ScriptEngineManager scriptEngineManager;
    private final LogReaderService logReaderService;
    private final HttpClient httpClient;
    private final BundleContext bundleContext;
    private final ConfigurationAdmin configAdmin;

    private static final String MCP_ONESHOT_TAG = "MCP-oneshot";
    private static final String MCP_FIRE_AT_KEY = "mcpFireAt";
    private static final long CLEANUP_INTERVAL_SECONDS = 60;

    private @Nullable McpSyncServer mcpServer;
    private final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();
    private @Nullable ScheduledFuture<?> cleanupTask;
    private volatile @Nullable McpCloudWebhookService cloudWebhook;
    private @Nullable LoggingTools loggingTools;
    private volatile McpConfiguration config = new McpConfiguration();

    /**
     * Optional dynamic reference to the openhab-core {@link WebhookService}; populated by the cloud
     * add-on (or any other provider) at runtime. Marked OPTIONAL because the MCP bundle is fully
     * functional on the local network without a cloud webhook. {@link McpCloudWebhookService}
     * resolves this via a supplier so it always sees the current value.
     */
    private volatile @Nullable WebhookService webhookService;

    @Activate
    public McpService(@Reference ItemRegistry itemRegistry, @Reference ItemBuilderFactory itemBuilderFactory,
            @Reference MetadataRegistry metadataRegistry, @Reference UserRegistry userRegistry,
            @Reference EventPublisher eventPublisher, @Reference ThingRegistry thingRegistry,
            @Reference RuleRegistry ruleRegistry, @Reference RuleManager ruleManager,
            @Reference ItemChannelLinkRegistry linkRegistry, @Reference ScriptEngineManager scriptEngineManager,
            @Reference LogReaderService logReaderService, @Reference HttpClientFactory httpClientFactory,
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
        this.scriptEngineManager = scriptEngineManager;
        this.logReaderService = logReaderService;
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
            new McpCloudWebhookService(() -> webhookService, SERVLET_PATH, httpClient).unregister();
            persistWebhookUrl("");
        }
        startServer();
    }

    private static boolean nonUrlFieldsChanged(McpConfiguration a, McpConfiguration b) {
        return a.enabled != b.enabled || a.exposeUntaggedItems != b.exposeUntaggedItems
                || a.maxItemsPerPage != b.maxItemsPerPage || a.resourceCoalesceMs != b.resourceCoalesceMs
                || a.enableFullApiAccess != b.enableFullApiAccess || a.enableScripting != b.enableScripting
                || a.enableLoggingAccess != b.enableLoggingAccess || a.enableUiDesign != b.enableUiDesign
                || a.enableStaticAssets != b.enableStaticAssets || a.registerCloudWebhook != b.registerCloudWebhook;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    void setWebhookService(WebhookService service) {
        this.webhookService = service;
        // For OPTIONAL/DYNAMIC references SCR may activate the component before binding the
        // service, so startServer's initial register() can run with webhookService == null and
        // give up. Retry now that the service is available.
        McpCloudWebhookService webhook = cloudWebhook;
        if (webhook != null && webhook.getPublicUrl() == null) {
            ThreadPoolManager.getScheduledPool("mcp").execute(() -> {
                if (webhook.getPublicUrl() != null) {
                    return;
                }
                if (webhook.register()) {
                    String url = webhook.getPublicUrl();
                    if (url != null) {
                        logger.debug("MCP cloud webhook URL registered after WebhookService bound: {}", url);
                        persistWebhookUrl(url);
                    }
                }
            });
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    void unsetWebhookService(WebhookService service) {
        if (this.webhookService == service) {
            this.webhookService = null;
        }
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
                webhook = new McpCloudWebhookService(() -> webhookService, SERVLET_PATH, httpClient);
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
                    transport::getSessionUsername, jsonMapper);
            SemanticTools semanticTools = new SemanticTools(itemRegistry, metadataRegistry, jsonMapper,
                    config.exposeUntaggedItems);
            ThingTools thingTools = new ThingTools(thingRegistry, linkRegistry, jsonMapper);
            RuleTools ruleTools = new RuleTools(ruleRegistry, ruleManager, jsonMapper, config.enableScripting);
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
                    .toolCall(itemTools.getManageItemTool(), (exchange, req) -> itemTools.handleManageItem(req))
                    .toolCall(itemTools.getSetItemTool(), (exchange, req) -> itemTools.handleSetItem(exchange, req))
                    .toolCall(thingTools.getThingsTool(), (exchange, req) -> thingTools.handleGetThings(req))
                    .toolCall(thingTools.getThingDetailsTool(),
                            (exchange, req) -> thingTools.handleGetThingDetails(req))
                    .toolCall(ruleTools.getRulesTool(), (exchange, req) -> ruleTools.handleGetRules(req))
                    .toolCall(ruleTools.getCreateRuleTool(), (exchange, req) -> ruleTools.handleCreateRule(req))
                    .toolCall(ruleTools.getUpdateRuleTool(), (exchange, req) -> ruleTools.handleUpdateRule(req))
                    .toolCall(ruleTools.getManageRuleTool(), (exchange, req) -> ruleTools.handleManageRule(req))
                    .toolCall(linkTools.getManageLinkTool(), (exchange, req) -> linkTools.handleManageLink(req))
                    .toolCall(systemTools.getSystemInfoTool(), (exchange, req) -> systemTools.handleGetSystemInfo(req))
                    .toolCall(systemTools.getHomeStatusTool(), (exchange, req) -> systemTools.handleGetHomeStatus(req))
                    .toolCall(watchTools.getWatchItemsTool(),
                            (exchange, req) -> watchTools.handleWatchItems(exchange, req))
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

            if (config.enableScripting) {
                ScriptTools scriptTools = new ScriptTools(scriptEngineManager, jsonMapper, true);
                builder = builder.toolCall(scriptTools.getExecuteScriptTool(),
                        (exchange, req) -> scriptTools.handleExecuteScript(req));
            }

            if (config.enableLoggingAccess) {
                LoggingTools logTools = new LoggingTools(logReaderService, httpClient, localBaseUrl,
                        transport::getSessionToken, scheduler, jsonMapper);
                this.loggingTools = logTools;
                builder = builder.toolCall(logTools.getReadLogsTool(), (exchange, req) -> logTools.handleGetLogs(req))
                        .toolCall(logTools.getManageLogLevelTool(),
                                (exchange, req) -> logTools.handleManageLogLevel(exchange, req));
            }

            if (config.enableUiDesign) {
                UiTools uiTools = new UiTools(httpClient, localBaseUrl, transport::getSessionToken, jsonMapper);
                builder = builder
                        .toolCall(uiTools.getListWidgetsTool(), (exchange, req) -> uiTools.handleListWidgets(req))
                        .toolCall(uiTools.getDescribeWidgetTool(), (exchange, req) -> uiTools.handleDescribeWidget(req))
                        .toolCall(uiTools.getPageSkeletonTool(), (exchange, req) -> uiTools.handleGetPageSkeleton(req))
                        .toolCall(uiTools.getManageUiComponentTool(),
                                (exchange, req) -> uiTools.handleManageUiComponent(exchange, req))
                        .toolCall(uiTools.getValidateUiComponentTool(),
                                (exchange, req) -> uiTools.handleValidateUiComponent(req));
            }

            if (config.enableStaticAssets) {
                Path htmlRoot = Path.of(OpenHAB.getConfigFolder(), "html");
                StaticAssetTools assetTools = new StaticAssetTools(htmlRoot, localBaseUrl, httpClient,
                        transport::getSessionToken, jsonMapper);
                builder = builder.toolCall(assetTools.getManageStaticAssetTool(),
                        (exchange, req) -> assetTools.handleManageStaticAsset(exchange, req));
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

        LoggingTools lt = loggingTools;
        if (lt != null) {
            lt.cancelPendingReverts();
            loggingTools = null;
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
                logger.debug("Error unregistering service: {}", e.getMessage(), e);
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
                items. Use set_item(action='command') to control devices (ON/OFF, dimmer \
                levels, etc); use set_item(action='state') to push a sensor reading or \
                seed a state without dispatching to the binding. \
                Item names are case-sensitive and must match exactly.

                Use manage_item to add, modify, or remove items (action='create'|'update'|'delete'). \
                Use manage_link to list, wire, or remove item-channel links \
                (action='get'|'create'|'delete').

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

                To monitor items for changes, call watch_items(action='start', itemNames=[...]) \
                with the item names you care about. Then when the user asks 'did anything \
                happen' or 'what changed', call get_events to retrieve the buffered list of \
                state changes since your last check. Stop with watch_items(action='stop') \
                (omit itemNames to stop everything). This is the recommended way to handle \
                'let me know when X happens' requests. (Advanced clients can also use \
                resources/subscribe on 'openhab://item/<name>' for push notifications.)

                Common patterns the user often asks for:
                - 'Remind me to do X in N minutes' or 'notify me at TIME': create_rule with a \
                  datetime trigger (use a relative offset like '+5m' or '+2h' for relative requests; \
                  the server resolves it against its own clock) plus a notification action. The \
                  notification should include proactive actionButtons for the likely responses \
                  (the affirmative action AND its opposite — see create_rule's actions schema).
                - 'Notify me when X happens' (event-driven): create_rule with an \
                  item_state_change or item_command trigger plus a notification action.
                - 'Every day at TIME do X' / 'on weekdays at TIME do X': create_rule with a \
                  time_of_day or cron trigger; add a day_of_week or ephemeris condition to \
                  scope it (weekdays, weekends, holidays).
                - 'Only do X if Y is also true': add a conditions array (item_state, \
                  time_of_day, etc.) to the rule.
                - 'Watch X for changes during this chat': watch_items(action='start', \
                  itemNames=[...]) now, get_events later in the same conversation.
                - 'What is the state of my home right now?': get_home_status.
                """;
        StringBuilder sb = new StringBuilder(base);
        if (config.enableFullApiAccess) {
            sb.append("""

                    For REST endpoints not covered by the curated tools above, use \
                    list_api_endpoints, describe_api_endpoint, and call_api. These give \
                    you access to the full openHAB REST API, including destructive \
                    endpoints (delete items, modify things, change service configs). \
                    Workflow: call list_api_endpoints to discover, describe_api_endpoint \
                    to understand the schema, then call_api to invoke. Prefer the curated \
                    tools when they cover the task — the meta-tools are the escape hatch.
                    """);
        }
        if (config.enableLoggingAccess) {
            sb.append("""

                    When the user reports a problem (thing offline, rule not firing, binding \
                    misbehaving), use get_logs to look at recent entries scoped to the relevant \
                    logger (e.g. loggerFilter='org\\\\.openhab\\\\.binding\\\\.<name>.*'). If the \
                    default verbosity doesn't show enough, briefly bump the level with \
                    manage_log_level(action='set') — it auto-reverts after 30 min unless you pass \
                    revertAfterSeconds=0. Ask the user to reproduce, then call get_logs again with \
                    sinceSequence from your earlier call to see only what's new. Use \
                    manage_log_level(action='get') to confirm or list current levels.
                    """);
        }
        if (config.enableUiDesign) {
            sb.append("""

                    For Main UI design (pages, widgets), use list_widgets to discover components, \
                    describe_widget(name) for the prop and slot schema of any component, \
                    get_page_skeleton(pageType) to start a new page with the right scaffold, and \
                    manage_ui_component for CRUD on pages (namespace='ui:page') and custom widgets \
                    (namespace='ui:widget'). Always call validate_ui_component before create/update \
                    to catch schema errors without a server round-trip. UI writes require an \
                    ADMIN-scoped token.

                    VISUAL VERIFICATION — strongly recommended for UI design. Designing pages \
                    without seeing the result is guesswork. At the START of any UI-design session: \
                    (1) check whether a browser-automation MCP server is connected in this session \
                    (Claude in Chrome, Playwright MCP, Puppeteer MCP, etc. — these expose tools like \
                    'screenshot', 'navigate', 'click'); (2) if one is connected, ASK THE USER for \
                    the URL they use to reach their openHAB Main UI (e.g. 'http://openhab.local:8080', \
                    'http://192.168.1.50:8080', or their myopenhab.org cloud URL) — the viewUrl this \
                    tool returns uses the SERVER's local hostname which is often 'localhost' and \
                    won't work from a remote browser. Save the user's URL as your base for the \
                    session. (3) After each manage_ui_component(action='create' or 'update'), \
                    substitute the user's base for the host in viewUrl/editUrl, navigate the browser \
                    there, screenshot, and iterate if it doesn't look right. The browser must already \
                    be signed in to openHAB; if it lands on the login page, ask the user to sign in \
                    once. If no browser tool is connected, fall back to telling the user the \
                    page-path (e.g. '/page/kitchen') and asking them to verify visually themselves.
                    """);
        }
        if (config.enableStaticAssets) {
            sb.append("""

                    For static assets (plan-page backgrounds, custom widget icons, CSS overrides) \
                    referenced from Main UI components, use manage_static_asset to list/get/put/delete \
                    files under $OPENHAB_CONF/html (served at /static/*). Upload binaries as base64; \
                    text files (css, json, svg, …) can use encoding='utf8'. Paths are relative — \
                    'plans/floor-1.png' lands at /static/plans/floor-1.png. Per-call uploads are \
                    capped at 10 MB and require an ADMIN-scoped token. Don't upload .html/.svg/.js \
                    files unless the user explicitly asked for one — they execute in the browser \
                    context of the Main UI.
                    """);
        }
        return sb.toString();
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
        cfg.enableScripting = toBoolean(properties.get("enableScripting"), cfg.enableScripting);
        cfg.enableLoggingAccess = toBoolean(properties.get("enableLoggingAccess"), cfg.enableLoggingAccess);
        cfg.enableUiDesign = toBoolean(properties.get("enableUiDesign"), cfg.enableUiDesign);
        cfg.enableStaticAssets = toBoolean(properties.get("enableStaticAssets"), cfg.enableStaticAssets);
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
