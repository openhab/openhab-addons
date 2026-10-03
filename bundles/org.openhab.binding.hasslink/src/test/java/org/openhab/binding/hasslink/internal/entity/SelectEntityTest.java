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
import org.openhab.binding.hasslink.internal.entity.impl.SelectEntity;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;

/**
 * Tests channel discovery, state options, and commands for Home Assistant select entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Select entity fixture contracts")
class SelectEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("select.speed"));
    }

    private final SelectEntity entity = new SelectEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SelectEntityTest#fixtures")
        @DisplayName("discovers supported channels")
        void discoversSupportedChannels(Fixture fixture) {
            EntityState state = fixture.load(SelectEntityTest.this);
            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));

            assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        }
    }

    @Nested
    @DisplayName("Channel state descriptions")
    class ChannelStateDescriptionTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SelectEntityTest#fixtures")
        @DisplayName("exposes selection options as state description options")
        void exposesOptionsFragment(Fixture fixture) {
            EntityState state = fixture.load(SelectEntityTest.this);
            List<String> options = Objects.requireNonNull(state.getAttributeAsList("options")).stream()
                    .map(Object::toString).toList();
            StateDescriptionFragment fragment = entity.getStateDescriptionFragment(state, EntityType.PRIMARY_ATTR,
                    context);
            List<String> describedOptions = Objects.requireNonNull(Objects.requireNonNull(fragment).getOptions())
                    .stream().map(StateOption::getValue).toList();

            assertThat(describedOptions, is(options));
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SelectEntityTest#fixtures")
        @DisplayName("dispatches active option state")
        void dispatchesFixtureState(Fixture fixture) {
            EntityState state = fixture.load(SelectEntityTest.this);

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR),
                    is(new ParsedData.StateData(new StringType(state.state()))));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.SelectEntityTest#fixtures")
        @DisplayName("maps select_option service call")
        void mapsSelectOptionService(Fixture fixture) {
            EntityState state = fixture.load(SelectEntityTest.this);
            List<String> options = Objects.requireNonNull(state.getAttributeAsList("options")).stream()
                    .map(Object::toString).toList();

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, new StringType(options.get(0)),
                            state, context).orElseThrow(),
                    "select", "select_option", fixture.entityId(), Map.of("option", options.get(0)));
        }
    }
}
