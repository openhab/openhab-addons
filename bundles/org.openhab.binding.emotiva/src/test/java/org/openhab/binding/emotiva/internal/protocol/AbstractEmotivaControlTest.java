/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emotiva.internal.protocol;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_FREQUENCY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TREBLE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_TUNER_CHANNEL;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.*;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_am;
import static org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands.band_fm;

import java.util.EnumMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.binding.emotiva.internal.EmotivaProcessorState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * Common setup for tests related to the {@link EmotivaControlCommands}.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class AbstractEmotivaControlTest {

    protected static final EnumMap<EmotivaControlCommands, String> MAP_SOURCES_MAIN_ZONE = new EnumMap<>(
            EmotivaControlCommands.class);
    protected static final EnumMap<EmotivaControlCommands, String> MAP_SOURCES_ZONE_2 = new EnumMap<>(
            EmotivaControlCommands.class);
    protected static final EnumMap<EmotivaControlCommands, String> CHANNEL_MAP = new EnumMap<>(
            EmotivaControlCommands.class);
    protected static final EnumMap<EmotivaControlCommands, String> RADIO_BAND_MAP = new EnumMap<>(
            EmotivaControlCommands.class);
    protected static final EmotivaProcessorState state = new EmotivaProcessorState();

    @BeforeAll
    static void beforeAll() {
        MAP_SOURCES_MAIN_ZONE.put(source_1, "Input 1");
        MAP_SOURCES_MAIN_ZONE.put(source_2, "SHIELD");
        MAP_SOURCES_MAIN_ZONE.put(hdmi1, "HDMI 1");
        MAP_SOURCES_MAIN_ZONE.put(coax1, "Coax 1");
        state.setSourcesMainZone(MAP_SOURCES_MAIN_ZONE);

        MAP_SOURCES_ZONE_2.put(source_1, "HDMI 1");
        MAP_SOURCES_ZONE_2.put(source_2, "SHIELD");
        MAP_SOURCES_ZONE_2.put(hdmi1, "HDMI 1");
        MAP_SOURCES_ZONE_2.put(zone2_coax1, "Coax 1");
        MAP_SOURCES_ZONE_2.put(zone2_ARC, "Audio Return Channel");
        MAP_SOURCES_ZONE_2.put(zone2_follow_main, "Follow Main");
        state.setSourcesZone2(MAP_SOURCES_ZONE_2);

        CHANNEL_MAP.put(channel_1, "Channel 1");
        CHANNEL_MAP.put(channel_2, "Channel 2");
        CHANNEL_MAP.put(channel_3, "My Radio Channel");
        state.setChannels(CHANNEL_MAP);

        RADIO_BAND_MAP.put(band_am, "AM");
        RADIO_BAND_MAP.put(band_fm, "FM");
        state.setTunerBands(RADIO_BAND_MAP);

        state.updateChannel(CHANNEL_TREBLE, new DecimalType(-3));
        state.updateChannel(CHANNEL_TUNER_CHANNEL, new StringType("FM    87.50MHz"));
        state.updateChannel(CHANNEL_FREQUENCY, QuantityType.valueOf(107.90, Units.HERTZ));
    }
}
