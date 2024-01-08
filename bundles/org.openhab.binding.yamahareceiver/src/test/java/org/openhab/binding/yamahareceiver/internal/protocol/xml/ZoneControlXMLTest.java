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
import static org.mockito.Mockito.verify;
import static org.openhab.binding.yamahareceiver.internal.TestModels.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone.Main_Zone;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;

/**
 * Unit test for {@link ZoneControlXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class ZoneControlXMLTest extends AbstractZoneControlXMLTest {

    private ZoneControlXML subject;

    private void given(String model) throws Exception {
        ctx.prepareForModel(model);

        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, deviceInformationState);
        deviceInformation.update();

        subject = new ZoneControlXML(con, Main_Zone, zoneConfig, zoneControlStateListener, deviceInformationState,
                () -> inputConverter);
    }

    @Test
    public void given_RX_S601D_when_power_then_sendsProperCommand() throws Exception {
        when_power_then_sendsProperCommand(RX_S601D);
    }

    @Test
    public void given_RX_V3900_when_power_then_sendsProperCommand() throws Exception {
        when_power_then_sendsProperCommand(RX_V3900);
    }

    private void when_power_then_sendsProperCommand(String model) throws Exception {
        given(model);

        // when
        subject.setPower(true);
        subject.setPower(false);

        // then
        verify(con).send(eq("<Main_Zone><Power_Control><Power>On</Power></Power_Control></Main_Zone>"));
        verify(con).send(eq("<Main_Zone><Power_Control><Power>Standby</Power></Power_Control></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_mute_then_sendsProperCommand() throws Exception {
        given(RX_S601D);

        // when
        subject.setMute(true);
        subject.setMute(false);

        // then
        verify(con).send(eq("<Main_Zone><Volume><Mute>On</Mute></Volume></Main_Zone>"));
        verify(con).send(eq("<Main_Zone><Volume><Mute>Off</Mute></Volume></Main_Zone>"));
    }

    @Test
    public void given_RX_V3900_when_mute_then_sendsProperCommand() throws Exception {
        given(RX_V3900);

        // when
        subject.setMute(true);
        subject.setMute(false);

        // then
        verify(con).send(eq("<Main_Zone><Vol><Mute>On</Mute></Vol></Main_Zone>"));
        verify(con).send(eq("<Main_Zone><Vol><Mute>Off</Mute></Vol></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_volume_then_sendsProperCommand() throws Exception {
        given(RX_S601D);

        // when
        subject.setVolumeDB(-2);

        // then
        verify(con).send(
                eq("<Main_Zone><Volume><Lvl><Val>-20</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume></Main_Zone>"));
    }

    @Test
    public void given_RX_V3900_when_volume_then_sendsProperCommand() throws Exception {
        given(RX_V3900);

        // when
        subject.setVolumeDB(-2);

        // then
        verify(con).send(eq("<Main_Zone><Vol><Lvl><Val>-20</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Vol></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_input_then_sendsProperCommand() throws Exception {
        when_input_then_sendsProperCommand(RX_S601D);
    }

    @Test
    public void given_RX_V3900_when_input_then_sendsProperCommand() throws Exception {
        when_input_then_sendsProperCommand(RX_V3900);
    }

    private void when_input_then_sendsProperCommand(String model) throws Exception {
        given(model);

        // when
        subject.setInput("HDMI1");

        // then
        verify(con).send(eq("<Main_Zone><Input><Input_Sel>HDMI1</Input_Sel></Input></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_surroundProgram_then_sendsProperCommand() throws Exception {
        given(RX_S601D);

        // when
        subject.setSurroundProgram("Adventure");

        // then
        verify(con).send(eq(
                "<Main_Zone><Surround><Program_Sel><Current><Sound_Program>Adventure</Sound_Program></Current></Program_Sel></Surround></Main_Zone>"));
    }

    @Test
    public void given_RX_V3900_when_surroundProgram_then_sendsProperCommand() throws Exception {
        given(RX_V3900);

        // when
        subject.setSurroundProgram("Adventure");

        // then
        verify(con).send(eq(
                "<Main_Zone><Surr><Pgm_Sel><Straight>Off</Straight><Pgm>Adventure</Pgm></Pgm_Sel></Surr></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_surroundProgramStraight_then_sendsProperCommand() throws Exception {
        given(RX_S601D);

        // when
        subject.setSurroundProgram("Straight");

        // then
        verify(con).send(eq(
                "<Main_Zone><Surround><Program_Sel><Current><Straight>On</Straight></Current></Program_Sel></Surround></Main_Zone>"));
    }

    @Test
    public void given_RX_V3900_when_surroundProgramStraight_then_sendsProperCommand() throws Exception {
        given(RX_V3900);

        // when
        subject.setSurroundProgram("Straight");

        // then
        verify(con).send(eq("<Main_Zone><Surr><Pgm_Sel><Straight>On</Straight></Pgm_Sel></Surr></Main_Zone>"));
    }

    @Test
    public void given_HTR_4069_when_dialogueLevel_then_sendsProperCommand() throws Exception {
        given(HTR_4069);

        // when
        subject.setDialogueLevel(10);

        // then
        verify(con).send(eq(
                "<Main_Zone><Sound_Video><Dialogue_Adjust><Dialogue_Lvl>10</Dialogue_Lvl></Dialogue_Adjust></Sound_Video></Main_Zone>"));
    }

    @Test
    public void given_RX_S601D_when_update_then_readsStateProperly() throws Exception {
        given(RX_S601D);

        // when
        subject.update();

        // then
        ArgumentCaptor<ZoneControlState> stateArg = ArgumentCaptor.forClass(ZoneControlState.class);
        verify(zoneControlStateListener).zoneStateChanged(stateArg.capture());

        ZoneControlState state = stateArg.getValue();
        assertNotNull(state);

        assertEquals(false, state.power);
        assertEquals(false, state.mute);
        assertEquals(-43, state.volumeDB, 0);
        assertEquals("Spotify", state.inputID);
        assertEquals("5ch Stereo", state.surroundProgram);
        // this model does not support dialogue level
        assertEquals(0, state.dialogueLevel);
    }

    @Test
    public void given_RX_V3900_when_update_then_readsStateProperly() throws Exception {
        given(RX_V3900);

        // when
        subject.update();

        // then
        ArgumentCaptor<ZoneControlState> stateArg = ArgumentCaptor.forClass(ZoneControlState.class);
        verify(zoneControlStateListener).zoneStateChanged(stateArg.capture());

        ZoneControlState state = stateArg.getValue();
        assertNotNull(state);

        assertEquals(true, state.power);
        assertEquals(false, state.mute);
        assertEquals(-46, state.volumeDB, 0);
        assertEquals("TV", state.inputID);
        assertEquals("2ch Stereo", state.surroundProgram);
        // this model does not support dialogue level
        assertEquals(0, state.dialogueLevel);
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
        assertEquals(-46, state.volumeDB, 0);
        assertEquals("TUNER", state.inputID);
        assertEquals("5ch Stereo", state.surroundProgram);
        assertEquals(1, state.dialogueLevel);
    }
}
