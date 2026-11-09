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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.ButtonEntity;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Tests channel discovery and state handling for Home Assistant button entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Button entity fixture contracts")
class ButtonEntityTest extends AbstractEntityTest {

    private static final String FIXTURE_PATH = COMPRESSED_FIXTURE_ROOT + "button.push.json";
    private static final String ENTITY_ID = "button.push";

    private final ButtonEntity entity = new ButtonEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    private EntityState fixtureState() {
        return loadFixtureState(FIXTURE_PATH, ENTITY_ID);
    }

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @Test
        @DisplayName("discovers only the primary action channel from the basic button fixture")
        void discoversOnlyPrimaryActionChannel() {
            List<ChannelSpec> specs = entity.getChannelSpecs(fixtureState(), context);

            assertThat(specs, hasSize(1));
            ChannelSpec spec = specs.get(0);
            assertThat(spec.attribute(), is(EntityType.PRIMARY_ATTR));
            assertThat(spec.itemType(), is(ItemType.STRING));
            assertThat(spec.kind(), is(ChannelKind.STATE));
            assertThat(spec.autoUpdatePolicy(), is(AutoUpdatePolicy.VETO));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @Test
        @DisplayName("does not dispatch the fixture's stateless unknown state")
        void ignoresRawFixtureState() {
            assertThat(fixtureState().state(), is("unknown"));
            assertThat(entity.parseState(fixtureState(), context), is(Map.of()));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @Test
        @DisplayName("maps PRESS to the Home Assistant button press service")
        void mapsPressCommand() {
            assertService(
                    entity.toServiceCall(ENTITY_ID, EntityType.PRIMARY_ATTR,
                            new StringType(ButtonEntity.BUTTON_PRESS_COMMAND), fixtureState(), context).orElseThrow(),
                    "button", "press", ENTITY_ID, null);
        }

        @Test
        @DisplayName("rejects unsupported commands and properties")
        void rejectsUnsupportedCommandsAndProperties() {
            EntityState state = fixtureState();

            assertThat(
                    entity.toServiceCall(ENTITY_ID, EntityType.PRIMARY_ATTR, new StringType("RELEASE"), state, context),
                    is(java.util.Optional.empty()));
            assertThat(entity.toServiceCall(ENTITY_ID, "options", new StringType(ButtonEntity.BUTTON_PRESS_COMMAND),
                    state, context), is(java.util.Optional.empty()));
        }
    }
}
