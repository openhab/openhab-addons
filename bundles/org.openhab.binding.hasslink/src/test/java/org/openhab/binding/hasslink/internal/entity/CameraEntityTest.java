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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.CameraEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Tests channel discovery, state handling, and commands for Home Assistant camera entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Camera entity fixture contracts")
class CameraEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("camera.demo_camera"), new Fixture("camera.demo_camera_png"),
                new Fixture("camera.demo_camera_without_stream"));
    }

    private final CameraEntity entity = new CameraEntity();
    private final HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
    private final EntityContext context = new EntityContext(bridge, attr -> true, (attribute, data) -> {
    });

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CameraEntityTest#fixtures")
        @DisplayName("discovers channels supported by each camera capability bitmask")
        void discoversFixtureBackedChannels(Fixture fixture) {
            EntityState state = fixture.load(CameraEntityTest.this);
            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);

            boolean hasStream = state.getAttributeAsLong("supported_features") == 3L;

            assertThat(specs.stream().map(ChannelSpec::attribute).toList(), //
                    is(hasStream //
                            ? containsInAnyOrder(EntityType.PRIMARY_ATTR, "power", "image", "stream_url")
                            : containsInAnyOrder(EntityType.PRIMARY_ATTR, "power", "image")));
            assertThat(specs, hasSize(hasStream ? 4 : 3));

            ChannelSpec power = specs.stream().filter(spec -> "power".equals(spec.attribute())).findFirst()
                    .orElseThrow();
            assertThat(power.itemType(), is(ItemType.SWITCH));
            assertThat(power.kind(), is(ChannelKind.STATE));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CameraEntityTest#fixtures")
        @DisplayName("maps primary and power state and asynchronously fetches the fixture image")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(CameraEntityTest.this);
            when(bridge.getRestBaseUri()).thenReturn("http://ha.example:8123");

            Map<String, ParsedData> parsed = entity.parseState(state, context);

            Map<String, ParsedData> expected = Map.of( //
                    EntityType.PRIMARY_ATTR, new ParsedData.StateData(new StringType("streaming")), //
                    "power", new ParsedData.StateData(OnOffType.ON));

            if (state.getAttributeAsLong("supported_features") == 3L) {
                String token = Objects.requireNonNull(state.getAttributeAsString("access_token"));
                expected = Map.of( //
                        EntityType.PRIMARY_ATTR, new ParsedData.StateData(new StringType("streaming")), //
                        "power", new ParsedData.StateData(OnOffType.ON), //
                        "stream_url",
                        new ParsedData.StateData(new StringType("http://ha.example:8123/api/camera_proxy_stream/"
                                + fixture.entityId() + "?token=" + token)));
            }
            assertThat(parsed, is(expected));

            String entityPicture = Objects.requireNonNull(state.getAttributeAsString("entity_picture"));
            verify(bridge).fetchImage(eq(entityPicture), isNull(), any());
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.CameraEntityTest#fixtures")
        @DisplayName("maps power commands to camera turn_on and turn_off services")
        void mapsPowerCommands(Fixture fixture) {
            EntityState state = fixture.load(CameraEntityTest.this);

            assertService(entity.toServiceCall(fixture.entityId(), "power", OnOffType.ON, state, context).orElseThrow(),
                    "camera", "turn_on", fixture.entityId(), null);
            assertService(
                    entity.toServiceCall(fixture.entityId(), "power", OnOffType.OFF, state, context).orElseThrow(),
                    "camera", "turn_off", fixture.entityId(), null);
        }
    }
}
