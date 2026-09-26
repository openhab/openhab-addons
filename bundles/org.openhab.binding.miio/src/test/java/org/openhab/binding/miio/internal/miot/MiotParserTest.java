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
package org.openhab.binding.miio.internal.miot;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoDeviceAction;
import org.openhab.binding.miio.internal.basic.MiIoDeviceActionCondition;
import org.openhab.binding.miio.internal.basic.OptionsValueListDTO;
import org.openhab.binding.miio.internal.basic.StateDescriptionDTO;

import com.google.gson.JsonParser;

/**
 * Test case for {@link MiotParser}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotParserTest {

    private static final String SPEC = """
            {
              "type": "urn:miot-spec-v2:device:vacuum:0000A006:test-model:1",
              "description": "Robot Cleaner",
              "services": [
                {
                  "iid": 2,
                  "type": "urn:miot-spec-v2:service:vacuum:00007810:test-model:1",
                  "description": "Robot Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:fault:00000009:test-model:1",
                      "description": "Device Fault", "format": "uint32", "access": ["read", "notify"],
                      "value-range": [0, 4294967295, 1] },
                    { "iid": 2, "type": "urn:miot-spec-v2:property:target-temperature:00000021:test-model:1",
                      "description": "Target Temperature", "format": "float", "access": ["read", "write"],
                      "unit": "celsius", "value-range": [16.5, 30, 0.5] }
                  ]
                },
                {
                  "iid": 8,
                  "type": "urn:miot-spec-v2:service:brush-cleaner:00007815:test-model:1",
                  "description": "Brush Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:brush-life-level:00000079:test-model:1",
                      "description": "Brush Life Level", "format": "uint8", "access": ["read", "notify"],
                      "unit": "percentage", "value-range": [0, 100, 1] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:reset-brush-life:00002830:test-model:1",
                      "description": "Reset Brush Life", "in": [], "out": [] }
                  ]
                },
                {
                  "iid": 9,
                  "type": "urn:miot-spec-v2:service:brush-cleaner:00007815:test-model:1",
                  "description": "Brush Cleaner",
                  "properties": [
                    { "iid": 1, "type": "urn:miot-spec-v2:property:brush-life-level:00000079:test-model:1",
                      "description": "Brush Life Level", "format": "uint8", "access": ["read", "notify"],
                      "unit": "percentage", "value-range": [0, 100, 1] }
                  ],
                  "actions": [
                    { "iid": 1, "type": "urn:miot-spec-v2:action:reset-brush-life:00002830:test-model:1",
                      "description": "Reset Brush Life", "in": [], "out": [] }
                  ]
                }
              ]
            }
            """;

    private MiIoBasicChannel parseChannel(String channelId) throws MiotParseException {
        MiotParser parser = new MiotParser("test.vacuum.model");
        List<MiIoBasicChannel> channels = parser.getDevice(JsonParser.parseString(SPEC)).getDevice().getChannels();
        return channels.stream().filter(c -> channelId.equals(c.getChannel())).findFirst().orElseThrow();
    }

    @Test
    public void valueRangeBeyondIntegerIsPreserved() throws MiotParseException {
        StateDescriptionDTO stateDescription = parseChannel("fault").getStateDescription();

        assertNotNull(stateDescription);
        assertEquals(new BigDecimal("4294967295"), stateDescription.getMaximum());
    }

    @Test
    public void decimalValueRangeIsPreserved() throws MiotParseException {
        StateDescriptionDTO stateDescription = parseChannel("target_temperature").getStateDescription();

        assertNotNull(stateDescription);
        assertEquals(new BigDecimal("16.5"), stateDescription.getMinimum());
        assertEquals(new BigDecimal("0.5"), stateDescription.getStep());
        assertEquals("%.1f %unit%", stateDescription.getPattern());
    }

    @Test
    public void actionsOfServicesWithSameTypeHaveUniqueValues() throws MiotParseException {
        MiIoBasicChannel actions = parseChannel("actions");
        StateDescriptionDTO stateDescription = actions.getStateDescription();

        assertNotNull(stateDescription);
        List<@Nullable String> values = stateDescription.getOptions().stream().map(OptionsValueListDTO::getValue)
                .toList();
        assertEquals(List.of("brush-cleaner-reset-brush-life", "brush-cleaner-reset-brush-life1"), values);

        List<String> matchValues = actions.getActions().stream().map(MiIoDeviceAction::getCondition)
                .map(Objects::requireNonNull).map((MiIoDeviceActionCondition c) -> c.getParameters().getAsJsonArray()
                        .get(0).getAsJsonObject().get("matchValue").getAsString())
                .toList();
        assertEquals(values, matchValues);
        assertEquals(List.of(8, 9), actions.getActions().stream().map(MiIoDeviceAction::getSiid).toList());
    }
}
