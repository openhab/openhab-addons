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
package org.openhab.binding.hasslink.internal.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.ScriptEntity;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery and execution commands for Home Assistant script entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Script entity contracts")
class ScriptEntityTest extends AbstractEntityTest {

    private static final String ENTITY_ID = "script.set_scene";
    private final ScriptEntity entity = new ScriptEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    private EntityState createSampleState() {
        return new EntityState(ENTITY_ID, "off", Map.of());
    }

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @Test
        @DisplayName("discovers primary script channel")
        void discoversSupportedChannels() {
            EntityState state = createSampleState();
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(1));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @Test
        @DisplayName("parses empty state map for stateless script entity")
        void dispatchesState() {
            EntityState state = createSampleState();

            assertThat(entity.parseState(state, context), is(Map.of()));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @Test
        @DisplayName("maps JSON payload command to script execution service call")
        void mapsScriptExecutionService() {
            EntityState state = createSampleState();

            assertService(
                    entity.toServiceCall(ENTITY_ID, EntityType.PRIMARY_ATTR, new StringType("{\"brightness\":42}"),
                            state, context).orElseThrow(),
                    "script", "set_scene", ENTITY_ID, Map.of("brightness", 42.0));
        }
    }
}
