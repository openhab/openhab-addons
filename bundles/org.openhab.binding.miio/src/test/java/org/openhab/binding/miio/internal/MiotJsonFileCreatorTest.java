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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.miot.MiotParseException;
import org.openhab.binding.miio.internal.miot.MiotParser;

import com.google.gson.JsonParser;

/**
 * Test case for {@link MiotJsonFileCreator}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotJsonFileCreatorTest {

    private static final String SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 11,
                  "type": "urn:miot-spec-v2:service:custom:00007801:test-model:1",
                  "description": "Custom",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:mute:00000001:test-model:1",
                      "description": "Mute", "format": "bool", "access": ["read", "write"] },
                    { "iid": 2, "type": "urn:miot-spec-v2:property:volume:00000002:test-model:1",
                      "description": "Volume", "format": "uint8", "access": ["read", "write"] }
                  ],
                  "actions": [
                    { "iid": 5, "type": "urn:miot-spec-v2:action:set-both:00002805:test-model:1",
                      "description": "Set Both", "in": [1, 2], "out": [] },
                    %s
                    { "iid": 7, "type": "urn:miot-spec-v2:action:set-missing:00002807:test-model:1",
                      "description": "Set Missing", "in": [9], "out": [] }
                  ]
                }
              ]
            }
            """;
    private static final String ACTION_WITHOUT_INPUT = """
            { "iid": 6, "type": "urn:miot-spec-v2:action:start-all:00002806:test-model:1",
              "description": "Start All", "in": [], "out": [] },
            """;

    private List<MiIoBasicChannel> createChannels(String action) throws MiotParseException {
        MiIoBasicDevice device = new MiotParser("test.vacuum.model")
                .getDevice(JsonParser.parseString(SPEC.formatted(action)));
        MiotJsonFileCreator.removeUnsupportedActions("test.vacuum.model", device);
        return device.getDevice().getChannels();
    }

    @Test
    public void actionsThatCanNotBeSentAreRemoved() throws MiotParseException {
        MiIoBasicChannel actions = createChannels(ACTION_WITHOUT_INPUT).stream()
                .filter(c -> "actions".equals(c.getChannel())).findFirst().orElseThrow();

        assertEquals(List.of(6), actions.getActions().stream().map(MiIoDeviceAction::getAiid).toList());
        assertEquals(List.of("custom-start-all"),
                Objects.requireNonNull(Objects.requireNonNull(actions.getStateDescription()).getOptions()).stream()
                        .map(OptionsValueListDTO::getValue).toList());
    }

    @Test
    public void channelWithoutActionLeftIsRemoved() throws MiotParseException {
        assertEquals(List.of("mute", "volume"), createChannels("").stream().map(MiIoBasicChannel::getChannel).toList());
    }
}
