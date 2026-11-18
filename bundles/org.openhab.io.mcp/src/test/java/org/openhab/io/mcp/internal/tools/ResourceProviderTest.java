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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.automation.RuleManager;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.io.mcp.internal.auth.McpUser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * Tests for {@link ResourceProvider}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ResourceProviderTest {

    private static final String THING_URI = "openhab://thing/hue:type:lamp";

    @Mock
    @Nullable
    ItemRegistry itemRegistry;

    @Mock
    @Nullable
    ThingRegistry thingRegistry;

    @Mock
    @Nullable
    ItemChannelLinkRegistry linkRegistry;

    @Mock
    @Nullable
    RuleRegistry ruleRegistry;

    @Mock
    @Nullable
    RuleManager ruleManager;

    @Mock
    @Nullable
    McpSyncServerExchange exchange;

    private @Nullable ResourceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ResourceProvider(Objects.requireNonNull(itemRegistry), null,
                Objects.requireNonNull(thingRegistry), Objects.requireNonNull(linkRegistry),
                Objects.requireNonNull(ruleRegistry), Objects.requireNonNull(ruleManager), newJsonMapper(), false);
    }

    @Test
    void thingResourceReadableByAdmin() throws Exception {
        Thing thing = createMockThing("lamp", "Lamp", ThingStatus.ONLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).get(thing.getUID())).thenReturn(thing);

        Map<String, Object> payload = readThing(new McpUser("alice", true));

        assertEquals("hue:type:lamp", payload.get("uid"));
        assertNull(payload.get("error"));
    }

    @Test
    void thingResourceRefusedForNonAdmin() throws Exception {
        Map<String, Object> payload = readThing(new McpUser("bob", false));

        assertTrue(String.valueOf(payload.get("error")).contains("administrator"));
        verifyNoInteractions(Objects.requireNonNull(thingRegistry));
    }

    private Map<String, Object> readThing(McpUser user) throws Exception {
        McpSyncServerExchange ex = Objects.requireNonNull(exchange);
        when(ex.transportContext()).thenReturn(McpTransportContext.create(Map.of(McpUser.CONTEXT_KEY, user)));
        SyncResourceTemplateSpecification spec = Objects.requireNonNull(provider).templates().stream()
                .filter(t -> t.resourceTemplate().uriTemplate().startsWith("openhab://thing/")).findFirst()
                .orElseThrow();
        ReadResourceResult result = spec.readHandler().apply(ex, new ReadResourceRequest(THING_URI));
        String json = ((TextResourceContents) result.contents().get(0)).text();
        return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }
}
