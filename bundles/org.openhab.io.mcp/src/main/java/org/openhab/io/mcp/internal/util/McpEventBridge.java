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
package org.openhab.io.mcp.internal.util;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.events.RuleStatusInfoEvent;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.events.ItemAddedEvent;
import org.openhab.core.items.events.ItemRemovedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ResourcesUpdatedNotification;

/**
 * Subscribes to openHAB core events and translates them into MCP
 * {@code notifications/resources/updated} pushes so clients that have called
 * {@code resources/subscribe} receive real-time updates.
 *
 * Register as an OSGi service when the MCP server is started; unregister on stop.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpEventBridge implements EventSubscriber {

    private static final Set<String> EVENT_TYPES = Set.of(ItemStateChangedEvent.TYPE, ItemAddedEvent.TYPE,
            ItemRemovedEvent.TYPE, ThingStatusInfoChangedEvent.TYPE, RuleStatusInfoEvent.TYPE);

    private final Logger logger = LoggerFactory.getLogger(McpEventBridge.class);

    private final McpSyncServer server;
    private final SubscriptionManager subscriptions;
    private final long coalesceMs;
    private final Map<String, Long> lastNotified = new ConcurrentHashMap<>();

    private static final int MAX_COALESCE_ENTRIES = 10_000;

    public McpEventBridge(McpSyncServer server, SubscriptionManager subscriptions, long coalesceMs) {
        this.server = server;
        this.subscriptions = subscriptions;
        this.coalesceMs = coalesceMs;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return EVENT_TYPES;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        try {
            if (event instanceof ItemStateChangedEvent e) {
                String uri = "openhab://item/" + e.getItemName();
                logger.debug("Event: item state changed → {}", uri);
                subscriptions.recordIfWatched(e.getItemName(), String.valueOf(e.getItemState()),
                        String.valueOf(e.getOldItemState()), Instant.now());
                notifyUpdated(uri);
            } else if (event instanceof ItemAddedEvent || event instanceof ItemRemovedEvent) {
                logger.debug("Event: item added/removed → list_changed");
                server.notifyResourcesListChanged();
            } else if (event instanceof ThingStatusInfoChangedEvent e) {
                String uri = "openhab://thing/" + e.getThingUID();
                logger.debug("Event: thing status changed → {}", uri);
                notifyUpdated(uri);
            } else if (event instanceof RuleStatusInfoEvent e) {
                String uri = "openhab://rule/" + e.getRuleId();
                logger.debug("Event: rule status changed → {}", uri);
                notifyUpdated(uri);
            }
        } catch (RuntimeException e) {
            logger.trace("Failed to translate event {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void notifyUpdated(String uri) {
        if (coalesceMs > 0) {
            long now = System.currentTimeMillis();
            Long prev = lastNotified.compute(uri, (key, existing) -> {
                if (existing != null && (now - existing) < coalesceMs) {
                    return existing;
                }
                return now;
            });
            if (prev != null && prev != now) {
                return;
            }
            if (lastNotified.size() > MAX_COALESCE_ENTRIES) {
                long threshold = now - coalesceMs;
                lastNotified.entrySet().removeIf(e -> e.getValue() < threshold);
            }
        }
        try {
            server.notifyResourcesUpdated(new ResourcesUpdatedNotification(uri));
        } catch (Exception e) {
            logger.trace("notifyResourcesUpdated failed for {}: {}", uri, e.getMessage());
        }
    }
}
