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
package org.openhab.io.mcp.internal.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.automation.RuleStatus;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.io.mcp.internal.util.SemanticModelBuilder;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * Builds MCP resource specifications + URI templates for openHAB items, things, and
 * rules. Paired with {@link org.openhab.io.mcp.internal.util.McpEventBridge} which emits
 * {@code notifications/resources/updated} when the underlying entities change state.
 *
 * URI scheme:
 * <ul>
 * <li>{@code openhab://item/<itemName>} — single item state/metadata (subscribable)</li>
 * <li>{@code openhab://thing/<thingUID>} — single thing status (subscribable)</li>
 * <li>{@code openhab://rule/<ruleUID>} — single rule status (subscribable)</li>
 * <li>{@code openhab://home/semantic-model} — full semantic tree (curated)</li>
 * </ul>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ResourceProvider {

    private static final String APPLICATION_JSON = "application/json";

    private final ItemRegistry itemRegistry;
    private final @Nullable MetadataRegistry metadataRegistry;
    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry linkRegistry;
    private final RuleRegistry ruleRegistry;
    private final RuleManager ruleManager;
    private final McpJsonMapper jsonMapper;
    private final boolean exposeUntaggedItems;

    /**
     * Creates a new {@code ResourceProvider} instance.
     *
     * @param itemRegistry the openHAB item registry for looking up items
     * @param metadataRegistry the metadata registry for item metadata, or {@code null} if unavailable
     * @param thingRegistry the thing registry for looking up things
     * @param linkRegistry the item-channel link registry for resolving channel-to-item links
     * @param ruleRegistry the automation rule registry for looking up rules
     * @param ruleManager the rule manager for querying rule status and enabled state
     * @param jsonMapper the MCP JSON mapper used to serialize resource payloads
     * @param exposeUntaggedItems whether to include items without semantic tags in the model
     */
    public ResourceProvider(ItemRegistry itemRegistry, @Nullable MetadataRegistry metadataRegistry,
            ThingRegistry thingRegistry, ItemChannelLinkRegistry linkRegistry, RuleRegistry ruleRegistry,
            RuleManager ruleManager, McpJsonMapper jsonMapper, boolean exposeUntaggedItems) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.thingRegistry = thingRegistry;
        this.linkRegistry = linkRegistry;
        this.ruleRegistry = ruleRegistry;
        this.ruleManager = ruleManager;
        this.jsonMapper = jsonMapper;
        this.exposeUntaggedItems = exposeUntaggedItems;
    }

    /**
     * Returns the list of curated top-level MCP resources.
     * Currently exposes the semantic model as a single discoverable resource.
     *
     * @return a list of synchronous resource specifications
     */
    public List<SyncResourceSpecification> resources() {
        return List.of(new SyncResourceSpecification(
                Resource.builder().uri("openhab://home/semantic-model").name("Semantic Model")
                        .description("Hierarchical view of locations, equipment, and points in the home.")
                        .mimeType(APPLICATION_JSON).build(),
                (ex, req) -> readSemanticModel(req.uri())));
    }

    /**
     * Returns URI templates for subscribable openHAB resources.
     * Provides templates for items, things, and rules that support real-time
     * subscription via {@code resources/subscribe}.
     *
     * @return a list of synchronous resource template specifications
     */
    public List<SyncResourceTemplateSpecification> templates() {
        return List.of(new SyncResourceTemplateSpecification(new ResourceTemplate("openhab://item/{itemName}",
                "openHAB Item", null,
                "Current state and metadata for an openHAB item. Subscribe to receive real-time notifications when the item's state changes.",
                APPLICATION_JSON, null), (ex, req) -> readItem(req.uri())),
                new SyncResourceTemplateSpecification(new ResourceTemplate("openhab://thing/{thingUID}",
                        "openHAB Thing", null,
                        "Status and channels of a Thing. Subscribe to receive notifications when the Thing's online/offline status changes.",
                        APPLICATION_JSON, null), (ex, req) -> readThing(req.uri())),
                new SyncResourceTemplateSpecification(new ResourceTemplate("openhab://rule/{ruleUID}", "openHAB Rule",
                        null,
                        "Status and last-fired info for an automation rule. Subscribe to receive notifications when the rule fires or its status changes.",
                        APPLICATION_JSON, null), (ex, req) -> readRule(req.uri())));
    }

    private ReadResourceResult readItem(String uri) {
        String itemName = uri.substring("openhab://item/".length());
        Item item = itemRegistry.get(itemName);
        if (item == null) {
            return toResult(uri, Map.of("error", "Item not found: " + itemName));
        }
        return toResult(uri, McpToolUtils.buildItemMap(item));
    }

    private ReadResourceResult readThing(String uri) {
        String uidStr = uri.substring("openhab://thing/".length());
        Thing thing = thingRegistry.get(new ThingUID(uidStr));
        if (thing == null) {
            return toResult(uri, Map.of("error", "Thing not found: " + uidStr));
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("uid", thing.getUID().toString());
        details.put("label", Objects.requireNonNullElse(thing.getLabel(), thing.getUID().toString()));
        details.put("bindingId", thing.getThingTypeUID().getBindingId());
        details.put("status", thing.getStatus().name());
        details.put("statusDetail", thing.getStatusInfo().getStatusDetail().name());
        details.put("statusDescription", Objects.requireNonNullElse(thing.getStatusInfo().getDescription(), ""));
        List<Map<String, Object>> channels = new ArrayList<>();
        thing.getChannels().forEach(ch -> {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("uid", ch.getUID().toString());
            c.put("acceptedItemType", Objects.requireNonNullElse(ch.getAcceptedItemType(), ""));
            c.put("linkedItems", linkRegistry.getLinkedItemNames(ch.getUID()).stream().sorted().toList());
            channels.add(c);
        });
        details.put("channels", channels);
        return toResult(uri, details);
    }

    private ReadResourceResult readRule(String uri) {
        String ruleUID = uri.substring("openhab://rule/".length());
        Rule rule = ruleRegistry.get(ruleUID);
        if (rule == null) {
            return toResult(uri, Map.of("error", "Rule not found: " + ruleUID));
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("uid", rule.getUID());
        details.put("name", Objects.requireNonNullElse(rule.getName(), ""));
        details.put("description", Objects.requireNonNullElse(rule.getDescription(), ""));
        details.put("tags", rule.getTags());
        RuleStatus status = ruleManager.getStatus(ruleUID);
        details.put("status", status != null ? status.name() : "UNKNOWN");
        details.put("enabled", Boolean.TRUE.equals(ruleManager.isEnabled(ruleUID)));
        details.put("triggerTypes", rule.getTriggers().stream().map(t -> t.getTypeUID()).toList());
        return toResult(uri, details);
    }

    private ReadResourceResult readSemanticModel(String uri) {
        SemanticModelBuilder builder = new SemanticModelBuilder(itemRegistry, metadataRegistry);
        Map<String, Object> model = builder.buildModel(exposeUntaggedItems);
        return toResult(uri, model);
    }

    private ReadResourceResult toResult(String uri, Map<String, ?> payload) {
        try {
            String json = jsonMapper.writeValueAsString(payload);
            return new ReadResourceResult(List.of(new TextResourceContents(uri, APPLICATION_JSON, json)));
        } catch (Exception e) {
            try {
                String errorJson = jsonMapper.writeValueAsString(
                        Map.of("error", "Failed to serialize resource: " + Objects.toString(e.getMessage(), "")));
                return new ReadResourceResult(List.of(new TextResourceContents(uri, APPLICATION_JSON, errorJson)));
            } catch (Exception ignored) {
                return new ReadResourceResult(List.of(new TextResourceContents(uri, APPLICATION_JSON,
                        "{\"error\":\"Failed to serialize resource\"}")));
            }
        }
    }
}
