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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.yamahareceiver.internal.TestModels.HTR_4069;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;

/**
 * Unit test for {@link ZoneBControlXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class ZoneBControlXMLTest extends AbstractZoneControlXMLTest {

    private ZoneBControlXML subject;

    private void given(String model) throws Exception {
        ctx.prepareForModel(model);
        when(con.sendReceive(eq("<Zone_2><Basic_Status>GetParam</Basic_Status></Zone_2>"))).thenReturn("<xml></xml>");

        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, deviceInformationState);
        deviceInformation.update();

        subject = new ZoneBControlXML(con, zoneConfig, zoneControlStateListener, deviceInformationState,
                () -> inputConverter);
    }

    @Test
    public void given_HTR_4069_when_power_then_sendsProperCommand() throws Exception {
        given(HTR_4069);

        // when
        subject.setPower(true);
        subject.setPower(false);

        // then
        verify(con).send(eq("<Main_Zone><Power_Control><Zone_B_Power>On</Zone_B_Power></Power_Control></Main_Zone>"));
        verify(con)
                .send(eq("<Main_Zone><Power_Control><Zone_B_Power>Standby</Zone_B_Power></Power_Control></Main_Zone>"));
    }

    @Test
    public void given_HTR_4069_when_mute_then_sendsProperCommand() throws Exception {
        given(HTR_4069);

        // when
        subject.setMute(true);
        subject.setMute(false);

        // then
        verify(con).send(eq("<Main_Zone><Volume><Zone_B><Mute>On</Mute></Zone_B></Volume></Main_Zone>"));
        verify(con).send(eq("<Main_Zone><Volume><Zone_B><Mute>Off</Mute></Zone_B></Volume></Main_Zone>"));
    }

    @Test
    public void given_HTR_4069_when_volume_then_sendsProperCommand() throws Exception {
        given(HTR_4069);

        // when
        subject.setVolumeDB(-2);

        // then
        verify(con).send(eq(
                "<Main_Zone><Volume><Zone_B><Lvl><Val>-20</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Zone_B></Volume></Main_Zone>"));
    }

    @Test
    public void given_HTR_4069_when_update_then_readsStateProperly() throws Exception {
        given(HTR_4069);

        // when
        subject.update();

        // then
        ArgumentCaptor<ZoneControlState> stateArg = ArgumentCaptor.forClass(ZoneControlState.class);
        verify(zoneControlStateListener).zoneStateChanged(stateArg.capture());

        ZoneControlState state = stateArg.getValue();
        assertNotNull(state);

        assertEquals(false, state.power);
        assertEquals(false, state.mute);
        assertEquals(-34.5, state.volumeDB, 0);
        assertEquals("TUNER", state.inputID);
        assertEquals("5ch Stereo", state.surroundProgram);
        assertEquals(1, state.dialogueLevel);
    }
}
