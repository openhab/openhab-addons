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
import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.HassLinkChannelFactory;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.RadioFrequencyEntity;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonPrimitive;

/**
 * Tests channel discovery, state mapping, and commands for Home Assistant radio frequency entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@DisplayName("Radio frequency entity contracts")
class RadioFrequencyEntityTest extends AbstractEntityTest {
    private static final String ENTITY_ID = "radio_frequency.garage_door";
    private final RadioFrequencyEntity entity = new RadioFrequencyEntity();
    private final EntityContext context = new EntityContext(mockBridgeForEntity(entity), attr -> true, null);

    @Test
    void discoversParsesAndMapsSendCommand() {
        EntityState state = new EntityState(ENTITY_ID, "2024-05-06T07:08:09+00:00",
                Map.of("frequency", new JsonPrimitive(433.92), "protocol", new JsonPrimitive("rtl_433")));
        List<Channel> channels = HassLinkChannelFactory.buildChannels(List.of(state),
                new ThingUID("hasslink", "fixture"), "Fixture", mockBridgeForEntity(entity));
        Map<String, ParsedData> parsed = entity.parseState(state, context);

        assertThat(channels, hasSize(entity.getChannelSpecs(state, context).size()));
        assertThat(parsed.get(EntityType.PRIMARY_ATTR), is(new ParsedData.StateData(new DateTimeType(state.state()))));
        assertThat(parsed.get("frequency"), is(new ParsedData.StateData(new DecimalType("433.92"))));
        assertThat(parsed.get("protocol"), is(new ParsedData.StateData(new StringType("rtl_433"))));
        assertService(entity.toServiceCall(ENTITY_ID, "send_command", new StringType("raw-payload"), state, context)
                .orElseThrow(), "radio_frequency", "send_command", ENTITY_ID, Map.of("command", "raw-payload"));
    }
}
