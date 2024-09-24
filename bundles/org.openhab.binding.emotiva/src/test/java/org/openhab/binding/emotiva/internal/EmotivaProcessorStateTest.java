/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_MODES;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_MAIN_ZONE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_SOURCES_ZONE_2;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_TUNER_BANDS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.MAP_TUNER_CHANNELS;

import java.util.EnumMap;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.core.library.types.DecimalType;

/**
 * Unit tests for the EmotivaProcessorHandlerState.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaProcessorStateTest {

    @Test
    void initialState() {
        var state = new EmotivaProcessorState();

        assertThat(state.getSourcesMainZone(), not(nullValue()));
        assertThat(state.getSourcesMainZone().size(), is(0));

        assertThat(state.getSourcesZone2(), not(nullValue()));
        assertThat(state.getSourcesZone2().size(), is(0));

        assertThat(state.getModes(), not(nullValue()));
        assertThat(state.getModes().size(), is(0));

        assertThat(state.getChannel(EmotivaSubscriptionTags.keepAlive), is(Optional.empty()));

        assertThat(state.getCommandMap(MAP_SOURCES_MAIN_ZONE).size(), is(0));
        assertThat(state.getCommandMap(MAP_SOURCES_ZONE_2).size(), is(0));
        assertThat(state.getCommandMap(MAP_TUNER_CHANNELS).size(), is(20));
        assertThat(state.getCommandMap(MAP_TUNER_BANDS).size(), is(2));
        assertThat(state.getCommandMap(MAP_MODES).size(), is(0));
    }

    @Test
    void updateAndRemoveChannel() {
        var state = new EmotivaProcessorState();

        assertThat(state.getChannel(EmotivaSubscriptionTags.keepAlive.getChannel()), is(Optional.empty()));

        state.updateChannel(EmotivaSubscriptionTags.keepAlive.getChannel(), new DecimalType(10));
        assertThat(state.getChannel(EmotivaSubscriptionTags.keepAlive.getChannel()),
                is(Optional.of(new DecimalType(10))));

        state.removeChannel(EmotivaSubscriptionTags.keepAlive.getChannel());
        assertThat(state.getChannel(EmotivaSubscriptionTags.keepAlive.getChannel()), is(Optional.empty()));
    }

    @Test
    void replaceSourcesMap() {
        var state = new EmotivaProcessorState();

        assertThat(state.getSourcesMainZone(), not(nullValue()));
        assertThat(state.getSourcesMainZone().size(), is(0));

        EnumMap<EmotivaControlCommands, String> sourcesMap = new EnumMap<>(EmotivaControlCommands.class);
        sourcesMap.put(EmotivaControlCommands.source_1, "HDMI1");
        state.setSourcesMainZone(sourcesMap);

        assertThat(state.getSourcesMainZone(), not(nullValue()));
        assertThat(state.getSourcesMainZone().size(), is(1));
        assertThat(state.getSourcesMainZone().get(EmotivaControlCommands.source_1), is("HDMI1"));
    }

    @Test
    void updateModes() {
        var state = new EmotivaProcessorState();

        state.updateModes(EmotivaSubscriptionTags.mode_auto, "Auto");

        assertThat(state.getModes(), not(nullValue()));
        assertThat(state.getModes().size(), is(1));
        assertThat(state.getModes().get(EmotivaSubscriptionTags.mode_auto), is("Auto"));

        state.updateModes(EmotivaSubscriptionTags.mode_auto, "Custom Label");

        assertThat(state.getModes(), not(nullValue()));
        assertThat(state.getModes().size(), is(1));
        assertThat(state.getModes().get(EmotivaSubscriptionTags.mode_auto), is("Custom Label"));
    }

    @Test
    void updateSourcesMap() {
        var state = new EmotivaProcessorState();

        EnumMap<EmotivaControlCommands, String> sourcesMap = new EnumMap<>(EmotivaControlCommands.class);
        sourcesMap.put(EmotivaControlCommands.source_1, "HDMI1");
        state.setSourcesMainZone(sourcesMap);

        assertThat(state.getSourcesMainZone(), not(nullValue()));
        assertThat(state.getSourcesMainZone().size(), is(1));
        assertThat(state.getSourcesMainZone().get(EmotivaControlCommands.source_1), is("HDMI1"));

        state.updateSourcesMainZone(EmotivaControlCommands.source_1, "SHIELD");

        assertThat(state.getSourcesMainZone(), not(nullValue()));
        assertThat(state.getSourcesMainZone().size(), is(1));
        assertThat(state.getSourcesMainZone().get(EmotivaControlCommands.source_1), is("SHIELD"));
    }
}
