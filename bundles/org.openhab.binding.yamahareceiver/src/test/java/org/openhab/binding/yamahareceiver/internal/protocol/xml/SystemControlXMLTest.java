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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.TestModels;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

/**
 * Unit test for {@link SystemControlXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class SystemControlXMLTest extends AbstractXMLProtocolTest {

    private SystemControlXML subject;

    private DeviceInformationState deviceInformationState;

    @Mock
    private SystemControlStateListener systemControlStateListener;

    protected void setupFor(String model) throws Exception {
        ctx.prepareForModel(model);
        ctx.respondWith("<System><Power_Control><Power>GetParam</Power></Power_Control></System>", "System_Power_Control_Power.xml");
        ctx.respondWith("<System><Party_Mode><Mode>GetParam</Mode></Party_Mode></System>", "System_Party_Mode_Mode.xml");

        deviceInformationState = new DeviceInformationState();
        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, deviceInformationState);
        deviceInformation.update();

        subject = new SystemControlXML(con, systemControlStateListener, deviceInformationState);
    }

    @Test
    public void given_RX_S601D_when_update_then_parsesState() throws Exception {
        // given
        setupFor(TestModels.RX_S601D);

        // when
        subject.update();

        // then
        ArgumentCaptor<SystemControlState> stateArg = ArgumentCaptor.forClass(SystemControlState.class);
        verify(systemControlStateListener, only()).systemControlStateChanged(stateArg.capture());

        SystemControlState state = stateArg.getValue();
        assertTrue(state.power);
        assertTrue(state.partyMode);
    }

    @Test
    public void given_RX_S601D_when_power_then_sendsProperCommand() throws Exception {
        // given
        setupFor(TestModels.RX_S601D);

        // when
        subject.setPower(true);
        subject.setPower(false);
        subject.setPartyMode(true);
        subject.setPartyModeMute(true);

        // then
        verify(con).send(eq("<System><Power_Control><Power>On</Power></Power_Control></System>"));
        verify(con).send(eq("<System><Power_Control><Power>Standby</Power></Power_Control></System>"));
        verify(con).send(eq("<System><Party_Mode><Mode>On</Mode></Party_Mode></System>"));
        verify(con).send(eq("<System><Party_Mode><Volume><Mute>On</Mute></Volume></Party_Mode></System>"));
    }

    @Test
    public void given_RX_V3900_when_partyMode_then_noCommandSend() throws Exception {
        // given
        setupFor(TestModels.RX_V3900);

        // when
        subject.setPartyMode(true);
        subject.setPartyModeMute(true);
        subject.setPartyModeVolume(true);

        // then
        verify(con, never()).send(anyString());
    }

    @Test
    public void given_RX_V3900_when_update_then_parsesStateAndDoesNotUpdateStateForPartyMode() throws Exception {
        // given
        setupFor(TestModels.RX_V3900);

        // when
        subject.update();

        // then
        ArgumentCaptor<SystemControlState> stateArg = ArgumentCaptor.forClass(SystemControlState.class);
        verify(systemControlStateListener, only()).systemControlStateChanged(stateArg.capture());
        verify(con, never()).sendReceive(eq("<System><Party_Mode><Mode>GetParam</Mode></Party_Mode></System>"));

        SystemControlState state = stateArg.getValue();
        assertTrue(state.power);
        assertFalse(state.partyMode);
    }

}
