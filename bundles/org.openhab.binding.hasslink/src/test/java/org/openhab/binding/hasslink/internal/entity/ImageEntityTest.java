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
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.ImageEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link ImageEntity} to verify channel discovery and state parsing
 * contracts using image entity fixtures.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Image entity fixture contracts")
class ImageEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("image.deebot_map", "../custom_fixtures/image.deebot_map.json"));
    }

    private final ImageEntity entity = new ImageEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ImageEntityTest#fixtures")
        @DisplayName("discovers single primary Image channel")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(ImageEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);
            String primaryId = fixture.entityId().replace('.', '-');
            List<String> channelIds = channels.stream().map(channel -> channel.getUID().getId()).toList();

            assertThat(channelIds, containsInAnyOrder(primaryId));
            assertThat(channels, hasSize(1));

            List<ChannelSpec> specs = entity.getChannelSpecs(state, context);
            assertThat(specs, hasSize(1));
            assertThat(specs.get(0).itemType(), is(ItemType.IMAGE));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ImageEntityTest#fixtures")
        @DisplayName("dispatches image state when primary channel is linked")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(ImageEntityTest.this);

            Map<String, ParsedData> parsed = entity.parseState(state, context);
            assertThat(parsed, is(notNullValue()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.ImageEntityTest#fixtures")
        @DisplayName("skips state parsing when primary channel is not linked")
        void skipsUnlinkedChannel(Fixture fixture) {
            EntityState state = fixture.load(ImageEntityTest.this);
            EntityContext unlinkedContext = new EntityContext(mockBridgeForEntity(entity), attr -> false, null);

            Map<String, ParsedData> parsed = entity.parseState(state, unlinkedContext);
            assertThat(parsed.isEmpty(), is(true));
        }
    }
}
