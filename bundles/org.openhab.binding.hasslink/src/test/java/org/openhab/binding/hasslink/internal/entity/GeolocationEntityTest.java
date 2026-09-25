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
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.GeolocationEntity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

/**
 * Tests channel discovery and state mapping for Home Assistant geolocation entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Geolocation entity fixture contracts")
class GeolocationEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of( //
                new Fixture("geo_location.fire_alarm"), //
                new Fixture("geo_location.thunderstorm"));
    }

    private final GeolocationEntity entity = new GeolocationEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.GeolocationEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(GeolocationEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.GeolocationEntityTest#fixtures")
        @DisplayName("dispatches distance, source, and location coordinates")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(GeolocationEntityTest.this);
            Map<String, ParsedData> parsed = entity.parseState(state, context);

            QuantityType<?> distance = QuantityType.valueOf(
                    state.state() + " " + Objects.requireNonNull(state.getAttributeAsString("unit_of_measurement")));
            PointType location = new PointType(
                    new DecimalType(Objects.requireNonNull(state.getAttributeAsDouble("latitude"))),
                    new DecimalType(Objects.requireNonNull(state.getAttributeAsDouble("longitude"))));

            assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(distance)));
            assertThat(parsed.get("source"), is(new ParsedData.StateData(
                    new StringType(Objects.requireNonNull(state.getAttributeAsString("source"))))));
            assertThat(parsed.get("location"), is(new ParsedData.StateData(location)));
        }
    }
}
