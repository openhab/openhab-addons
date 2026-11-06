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
import static org.hamcrest.Matchers.contains;
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
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.AlarmControlPanelEntity;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.CommandOption;

/**
 * Tests channel, state, and command behavior for Home Assistant alarm control panel entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("Alarm control panel entity fixture contracts")
class AlarmControlPanelEntityTest extends AbstractEntityTest {

    static Stream<Fixture> fixtures() {
        return Stream.of(new Fixture("alarm_control_panel.security"));
    }

    private final AlarmControlPanelEntity entity = new AlarmControlPanelEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Nested
    @DisplayName("Channel discovery")
    class ChannelDiscoveryTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("creates only the primary command channel for each fixture")
        void createsExpectedChannels(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);
            HassLinkBridgeHandler bridge = mockBridgeForEntity(entity);

            List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                    new ThingUID("hasslink", "fixture"), "Fixture", bridge);

            assertThat(channels.stream().map(channel -> channel.getUID().getId()).toList(),
                    containsInAnyOrder(fixture.entityId().replace('.', '-')));
            assertThat(channels, hasSize(1));
        }
    }

    @Nested
    @DisplayName("Channel state descriptions and command options")
    class ChannelStateDescriptionAndCommandOptionsTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("exposes disarm and supported alarm features as primary command options")
        void exposesSupportedFeatures(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);
            List<CommandOption> options = entity.getCommandOptions(state, EntityType.PRIMARY_ATTR, context);

            assertThat(options, is(notNullValue()));
            assertThat(options, contains( //
                    new CommandOption("disarm", "Disarm"), //
                    new CommandOption("arm", "Arm"), //
                    new CommandOption("arm_home", "Arm Home"), //
                    new CommandOption("arm_night", "Arm Night"), //
                    new CommandOption("arm_custom_bypass", "Arm Custom Bypass"), //
                    new CommandOption("arm_vacation", "Arm Vacation") //
            ));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("returns null descriptions and options for unsupported attributes")
        void returnsNullForUnsupportedAttributes(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);

            assertRejectsUnsupportedAttributes(entity, context, state);
            assertNoCommandOptions(entity, context, state, "code_format");
        }
    }

    @Nested
    @DisplayName("Inbound state dispatch")
    class InboundStateDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("dispatches the raw Home Assistant alarm state")
        void dispatchesState(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);

            ParsedData expected = new ParsedData.StateData(new StringType(state.state()));

            assertThat(entity.parseState(state, context).get(EntityType.PRIMARY_ATTR), is(expected));
        }
    }

    @Nested
    @DisplayName("Outbound command dispatch")
    class OutboundCommandDispatchTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("maps every supported alarm command to its Home Assistant service")
        void mapsSupportedCommands(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);
            Map<String, String> services = Map.of( //
                    "disarm", "alarm_disarm", //
                    "arm", "alarm_arm_away", //
                    "arm_away", "alarm_arm_away", //
                    "arm_home", "alarm_arm_home", //
                    "arm_night", "alarm_arm_night", //
                    "arm_custom_bypass", "alarm_arm_custom_bypass", //
                    "arm_vacation", "alarm_arm_vacation", //
                    "trigger", "alarm_trigger");

            for (Map.Entry<String, String> service : services.entrySet()) {
                assertService(
                        entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR,
                                new StringType(service.getKey()), state, context).orElseThrow(),
                        "alarm_control_panel", service.getValue(), fixture.entityId(), null);
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.openhab.binding.hasslink.internal.entity.AlarmControlPanelEntityTest#fixtures")
        @DisplayName("maps an inline alarm code into the Home Assistant payload")
        void mapsInlineCode(Fixture fixture) {
            EntityState state = fixture.load(AlarmControlPanelEntityTest.this);

            assertService(
                    entity.toServiceCall(fixture.entityId(), EntityType.PRIMARY_ATTR, new StringType("arm:1234"), state,
                            context).orElseThrow(),
                    "alarm_control_panel", "alarm_arm_away", fixture.entityId(), Map.of("code", "1234"));
        }
    }
}
