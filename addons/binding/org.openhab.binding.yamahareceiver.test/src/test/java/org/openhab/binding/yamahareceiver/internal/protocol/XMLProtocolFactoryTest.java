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
package org.openhab.binding.yamahareceiver.internal.protocol;

import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.*;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone.Zone_2;

/**
 * Unit test for {@link ProtocolFactory}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class XMLProtocolFactoryTest extends AbstractXMLProtocolTest {

    @Mock
    private YamahaZoneConfig zoneConfig;

    @Mock
    private ZoneControlStateListener zoneControlStateListener;

    private DeviceInformationState state = new DeviceInformationState();

    private XMLProtocolFactory subject;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        when(zoneConfig.getZone()).thenReturn(Zone_2);
        when(con.sendReceive(eq("<Zone_2><Basic_Status>GetParam</Basic_Status></Zone_2>"))).thenReturn("<xml></xml>");

        subject = new XMLProtocolFactory();
    }

    @Test
    public void given_HTR4069_with_ZONEB_then_Zone2_control_is_ZoneBControlXML() throws IOException, ReceivedMessageParseException {

        // arrange
        ctx.prepareForModel("HTR-4069");

        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, state);
        deviceInformation.update();

        // act
        ZoneControl zoneControl = subject.ZoneControl(con, zoneConfig, zoneControlStateListener, () -> null, state);

        // assert
        assertTrue("Created ZoneB control", zoneControl instanceof ZoneBControlXML);
    }

    @Test
    public void given_RXS601D_without_ZONEB_then_Zone2_control_is_ZoneControlXML() throws IOException, ReceivedMessageParseException {

        // arrange
        ctx.prepareForModel("RX-S601D");

        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, state);
        deviceInformation.update();

        // act
        ZoneControl zoneControl = subject.ZoneControl(con, zoneConfig, zoneControlStateListener, () -> null, state);

        // assert
        assertTrue("Created Zone control", zoneControl instanceof ZoneControlXML);
    }

}
