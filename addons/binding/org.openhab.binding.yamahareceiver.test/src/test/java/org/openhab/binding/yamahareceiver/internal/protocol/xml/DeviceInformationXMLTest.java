/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.junit.Test;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone.*;

/**
 * Unit test for {@link DeviceInformationXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class DeviceInformationXMLTest extends AbstractXMLProtocolTest {

    private DeviceInformationState state;

    private DeviceInformationXML subject;

    @Override
    public void onSetUp() {
        state = new DeviceInformationState();
        subject = new DeviceInformationXML(con, state);
    }

    @Test
    public void when_HTR4069_then_detects_featureZoneB_and_addsZone2() throws IOException, ReceivedMessageParseException {

        // arrange
        ctx.prepareForModel("HTR-4069");

        // act
        subject.update();

        // assert
        assertTrue("ZONE_B detected", state.features.contains(ZONE_B));
        assertTrue("Zone_2 added", state.zones.contains(Zone_2));
    }

    @Test
    public void when_RXV3900_then_detects_features_and_zones_from_descriptor() throws IOException, ReceivedMessageParseException {

        // arrange
        ctx.prepareForModel("RX-V3900");

        // act
        subject.update();

        // assert
        assertTrue("Zones detected", state.zones.containsAll(Arrays.asList(Main_Zone, Zone_2, Zone_3)));
        assertTrue("Features detected", state.features.containsAll(Arrays.asList(TUNER, BLUETOOTH)));
    }


}
