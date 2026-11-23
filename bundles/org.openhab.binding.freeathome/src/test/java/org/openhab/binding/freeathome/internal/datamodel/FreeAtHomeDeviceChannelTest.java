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
package org.openhab.binding.freeathome.internal.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.openhab.binding.freeathome.internal.valuestateconverter.ValueStateConverter;
import org.openhab.core.library.types.OnOffType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests where the eco channel of a room temperature controller reads its state from.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class FreeAtHomeDeviceChannelTest {

    private static final String ECO_MODE_LABEL = "pid-eco-mode-on-off-request";
    private static final String STATUS_INDICATION_LABEL = "pid-status-indication";

    private static final String STATUS_COMFORT = "65";
    private static final String STATUS_ECO = "68";
    private static final String STATUS_COMFORT_HEATING = "33";

    @Test
    public void ecoChannelReadsTheStatusBitWhenTheControllerHasNoEcoIndication() throws FreeAtHomeGeneralException {
        FreeAtHomeDeviceChannel channel = roomTemperatureController("""
                "inputs": { "idp0011": { "pairingID": 58 } },
                "outputs": { "odp0009": { "pairingID": 54 } }
                """);

        FreeAtHomeDatapointGroup eco = onlyGroupLabelled(channel, ECO_MODE_LABEL);
        ValueStateConverter converter = eco.getValueStateConverter();

        assertEquals("odp0009", outputDatapointId(eco));
        assertEquals("idp0011", inputDatapointId(eco));
        assertEquals(OnOffType.ON, converter.convertToState(STATUS_ECO));
        assertEquals(OnOffType.OFF, converter.convertToState(STATUS_COMFORT));
        assertEquals(OnOffType.OFF, converter.convertToState(STATUS_COMFORT_HEATING));
        assertEquals(OnOffType.ON, converter.convertToState(STATUS_ECO + ".0"));
        assertEquals("1", converter.convertToValueString(OnOffType.ON));
        assertEquals("0", converter.convertToValueString(OnOffType.OFF));
    }

    @Test
    public void ecoChannelKeepsTheEcoIndicationWhenTheControllerHasOne() throws FreeAtHomeGeneralException {
        FreeAtHomeDeviceChannel channel = roomTemperatureController("""
                "inputs": { "idp0011": { "pairingID": 58 } },
                "outputs": { "odp0009": { "pairingID": 54 }, "odp0020": { "pairingID": 68 } }
                """);

        FreeAtHomeDatapointGroup eco = onlyGroupLabelled(channel, ECO_MODE_LABEL);

        assertEquals("odp0020", outputDatapointId(eco));
        assertEquals(OnOffType.ON, eco.getValueStateConverter().convertToState("1"));
    }

    @Test
    public void controllerWithoutEcoRequestGetsNoSecondStatusChannel() throws FreeAtHomeGeneralException {
        FreeAtHomeDeviceChannel channel = roomTemperatureController("""
                "inputs": {},
                "outputs": { "odp0009": { "pairingID": 54 } }
                """);

        assertEquals(0, groupsLabelled(channel, ECO_MODE_LABEL).size());
        assertEquals(1, groupsLabelled(channel, STATUS_INDICATION_LABEL).size());
    }

    @Test
    public void virtualControllerKeepsReceivingTheEcoRequest() throws FreeAtHomeGeneralException {
        FreeAtHomeDeviceChannel channel = roomTemperatureController("""
                "inputs": { "idp0011": { "pairingID": 58 } },
                "outputs": { "odp0009": { "pairingID": 54 } }
                """);

        channel.applyChangesForVirtualDevice();
        FreeAtHomeDatapointGroup eco = onlyGroupLabelled(channel, ECO_MODE_LABEL);

        assertEquals("idp0011", outputDatapointId(eco));
        assertNull(eco.getInputDatapoint());
        assertEquals(OnOffType.ON, eco.getValueStateConverter().convertToState("1"));
    }

    private static FreeAtHomeDeviceChannel roomTemperatureController(String datapoints) {
        JsonObject channels = JsonParser.parseString("""
                { "ch0000": { "functionID": "23", "displayName": "RTC", %s } }
                """.formatted(datapoints)).getAsJsonObject();
        FreeAtHomeDeviceChannel channel = new FreeAtHomeDeviceChannel();
        channel.createChannelFromJson("RTC", "ch0000", channels, false, false);
        return channel;
    }

    private static List<FreeAtHomeDatapointGroup> groupsLabelled(FreeAtHomeDeviceChannel channel, String label)
            throws FreeAtHomeGeneralException {
        List<FreeAtHomeDatapointGroup> groups = new ArrayList<>();
        for (int i = 0; i < channel.getNumberOfDatapointGroup(); i++) {
            FreeAtHomeDatapointGroup group = channel.getDatapointGroup(i);
            if (label.equals(group.getLabel())) {
                groups.add(group);
            }
        }
        return groups;
    }

    private static FreeAtHomeDatapointGroup onlyGroupLabelled(FreeAtHomeDeviceChannel channel, String label)
            throws FreeAtHomeGeneralException {
        List<FreeAtHomeDatapointGroup> groups = groupsLabelled(channel, label);
        assertEquals(1, groups.size());
        return groups.get(0);
    }

    private static String outputDatapointId(FreeAtHomeDatapointGroup group) {
        return datapointId(group.getOutputDatapoint());
    }

    private static String inputDatapointId(FreeAtHomeDatapointGroup group) {
        return datapointId(group.getInputDatapoint());
    }

    private static String datapointId(@Nullable FreeAtHomeDatapoint datapoint) {
        return Objects.requireNonNull(datapoint).getDatapointId();
    }
}
