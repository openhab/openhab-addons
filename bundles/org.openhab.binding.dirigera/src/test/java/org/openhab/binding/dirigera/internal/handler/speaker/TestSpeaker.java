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
package org.openhab.binding.dirigera.internal.handler.speaker;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestSpeaker} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestSpeaker {
    @Test
    void testSpeakerDevice() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_SPEAKER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        SpeakerHandler handler = new SpeakerHandler(thing, SPEAKER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "338bb721-35bb-4775-8cd0-ba70fc37ab10_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkSpeakerStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CROSSFADE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SHUFFLE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_MUTE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_REPEAT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VOLUME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_TRACK), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_IMAGE), RefreshType.REFRESH);
        checkSpeakerStates(callback);
    }

    void checkSpeakerStates(CallbackMock callback) {
        State crossfadeState = callback.getState("dirigera:speaker:test-device:crossfade");
        assertNotNull(crossfadeState);
        assertTrue(crossfadeState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((crossfadeState)), "Crossfade Off");
        State shuffleState = callback.getState("dirigera:speaker:test-device:shuffle");
        assertNotNull(shuffleState);
        assertTrue(shuffleState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((shuffleState)), "Shuffle Off");
        State muteState = callback.getState("dirigera:speaker:test-device:mute");
        assertNotNull(muteState);
        assertTrue(muteState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((muteState)), "Mute Off");
        State repeatState = callback.getState("dirigera:speaker:test-device:repeat");
        assertNotNull(repeatState);
        assertTrue(repeatState instanceof DecimalType);
        assertEquals(0, ((DecimalType) repeatState).intValue(), "Repeat setting");
        State volumeState = callback.getState("dirigera:speaker:test-device:volume");
        assertNotNull(volumeState);
        assertTrue(volumeState instanceof PercentType);
        assertEquals(16, ((PercentType) volumeState).intValue(), "Volume");
        State trackState = callback.getState("dirigera:speaker:test-device:media-title");
        assertNotNull(trackState);
        assertTrue(trackState instanceof StringType);
        assertTrue(((StringType) trackState).toFullString().startsWith("The Anjunadeep Edition"));
        State pictureState = callback.getState("dirigera:speaker:test-device:image");
        assertNotNull(pictureState);
        assertTrue(pictureState instanceof RawType);
    }
}
