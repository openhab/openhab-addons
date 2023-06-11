/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.Commands.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolService.getZoneResponse;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * The zone protocol class is used to control one zone of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link ZoneControlState} instead.
 *
 * @author David Gräff - Refactored
 * @author Eric Thill
 * @author Ben Jones
 * @author Tomasz Maruszak - Refactoring, input mapping fix, added Straight surround, volume DB fix and config
 *         improvement.
 */
public class ZoneControlXML implements ZoneControl {

    protected Logger logger = LoggerFactory.getLogger(ZoneControlXML.class);

    private static final String SURROUND_PROGRAM_STRAIGHT = "Straight";

    private final ZoneControlStateListener observer;
    private final Supplier<InputConverter> inputConverterSupplier;
    private final WeakReference<AbstractConnection> comReference;
    private final Zone zone;
    private final YamahaZoneConfig zoneConfig;
    private final DeviceDescriptorXML.ZoneDescriptor zoneDescriptor;

    protected CommandTemplate power = new CommandTemplate("<Power_Control><Power>%s</Power></Power_Control>",
            "Power_Control/Power");
    protected CommandTemplate mute = new CommandTemplate("<Volume><Mute>%s</Mute></Volume>", "Volume/Mute");
    protected CommandTemplate volume = new CommandTemplate(
            "<Volume><Lvl><Val>%d</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume>", "Volume/Lvl/Val");
    protected CommandTemplate inputSel = new CommandTemplate("<Input><Input_Sel>%s</Input_Sel></Input>",
            "Input/Input_Sel");
    protected String inputSelNamePath = "Input/Input_Sel_Item_Info/Title";
    protected CommandTemplate surroundSelProgram = new CommandTemplate(
            "<Surround><Program_Sel><Current><Sound_Program>%s</Sound_Program></Current></Program_Sel></Surround>",
            "Surround/Program_Sel/Current/Sound_Program");
    protected CommandTemplate surroundSelStraight = new CommandTemplate(
            "<Surround><Program_Sel><Current><Straight>On</Straight></Current></Program_Sel></Surround>",
            "Surround/Program_Sel/Current/Straight");
    protected CommandTemplate sceneSel = new CommandTemplate("<Scene><Scene_Sel>%s</Scene_Sel></Scene>");
    protected boolean sceneSelSupported = false;
    protected CommandTemplate dialogueLevel = new CommandTemplate(
            "<Sound_Video><Dialogue_Adjust><Dialogue_Lvl>%d</Dialogue_Lvl></Dialogue_Adjust></Sound_Video>",
            "Sound_Video/Dialogue_Adjust/Dialogue_Lvl");
    protected CommandTemplate hdmi1Out = new CommandTemplate(
            "<System><Sound_Video><HDMI><Output><OUT_1>%s</OUT_1></Output></HDMI></Sound_Video></System>",
            "Sound_Video/HDMI/Output/OUT_1");
    protected CommandTemplate hdmi2Out = new CommandTemplate(
            "<System><Sound_Video><HDMI><Output><OUT_2>%s</OUT_2></Output></HDMI></Sound_Video></System>",
            "Sound_Video/HDMI/Output/OUT_2");
    protected boolean dialogueLevelSupported = false;
    protected boolean hdmi1OutSupported = false;
    protected boolean hdmi2OutSupported = false;

    public ZoneControlXML(AbstractConnection con, Zone zone, YamahaZoneConfig zoneSettings,
            ZoneControlStateListener observer, DeviceInformationState deviceInformationState,
            Supplier<InputConverter> inputConverterSupplier) {
        this.comReference = new WeakReference<>(con);
        this.zone = zone;
        this.zoneConfig = zoneSettings;
        this.zoneDescriptor = DeviceDescriptorXML.getAttached(deviceInformationState).zones.getOrDefault(zone, null);
        this.observer = observer;
        this.inputConverterSupplier = inputConverterSupplier;

        this.applyModelVariations();
    }

    /**
     * Apply command changes to ensure compatibility with all supported models
     */
    protected void applyModelVariations() {
        if (zoneDescriptor == null) {
            logger.trace("Zone {} - descriptor not available", getZone());
            return;
        }

        logger.trace("Zone {} - compatibility detection", getZone());

        // Note: Detection if scene is supported
        sceneSelSupported = zoneDescriptor.hasCommandEnding("Scene,Scene_Sel", () -> logger
                .debug("Zone {} - the {} channel is not supported on your model", getZone(), CHANNEL_SCENE));

        // Note: Detection if dialogue level is supported
        dialogueLevelSupported = zoneDescriptor.hasAnyCommandEnding("Sound_Video,Dialogue_Adjust,Dialogue_Lvl",
                "Sound_Video,Dialogue_Adjust,Dialogue_Lift");
        if (zoneDescriptor.hasCommandEnding("Sound_Video,Dialogue_Adjust,Dialogue_Lift")) {
            dialogueLevel = dialogueLevel.replace("Dialogue_Lvl", "Dialogue_Lift");
            logger.debug("Zone {} - adjusting command to: {}", getZone(), dialogueLevel);
        }
        if (!dialogueLevelSupported) {
            logger.debug("Zone {} - the {} channel is not supported on your model", getZone(), CHANNEL_DIALOGUE_LEVEL);
        }

        hdmi1OutSupported = zoneDescriptor.hasCommandEnding("Sound_Video,HDMI,Output,OUT_1", () -> logger
                .debug("Zone {} - the {} channel is not supported on your model", getZone(), CHANNEL_HDMI1OUT));

        hdmi2OutSupported = zoneDescriptor.hasCommandEnding("Sound_Video,HDMI,Output,OUT_2", () -> logger
                .debug("Zone {} - the {} channel is not supported on your model", getZone(), CHANNEL_HDMI2OUT));

        // Note: Detection for RX-V3900, which uses <Vol> instead of <Volume>
        if (zoneDescriptor.hasCommandEnding("Vol,Lvl")) {
            volume = volume.replace("Volume", "Vol");
            logger.debug("Zone {} - adjusting command to: {}", getZone(), volume);
        }
        if (zoneDescriptor.hasCommandEnding("Vol,Mute")) {
            mute = mute.replace("Volume", "Vol");
            logger.debug("Zone {} - adjusting command to: {}", getZone(), mute);
        }

        try {
            // Note: Detection for RX-V3900, which has a different XML node for surround program
            Node basicStatusNode = getZoneResponse(comReference.get(), getZone(), ZONE_BASIC_STATUS_CMD,
                    ZONE_BASIC_STATUS_PATH);
            String surroundProgram = getNodeContentOrEmpty(basicStatusNode, "Surr/Pgm_Sel/Pgm");

            if (!surroundProgram.isEmpty()) {
                surroundSelProgram = new CommandTemplate(
                        "<Surr><Pgm_Sel><Straight>Off</Straight><Pgm>%s</Pgm></Pgm_Sel></Surr>", "Surr/Pgm_Sel/Pgm");
                logger.debug("Zone {} - adjusting command to: {}", getZone(), surroundSelProgram);

                surroundSelStraight = new CommandTemplate("<Surr><Pgm_Sel><Straight>On</Straight></Pgm_Sel></Surr>",
                        "Surr/Pgm_Sel/Straight");
                logger.debug("Zone {} - adjusting command to: {}", getZone(), surroundSelStraight);
            }

        } catch (ReceivedMessageParseException | IOException e) {
            logger.debug("Could not perform feature detection for RX-V3900");
        }
    }

    protected void sendCommand(String message) throws IOException {
        comReference.get().send(XMLUtils.wrZone(zone, message));
    }

    protected void sendCommandWithoutZone(String message) throws IOException {
        comReference.get().send(message);
    }

    /**
     * Return the zone
     */
    public Zone getZone() {
        return zone;
    }

    @Override
    public void setPower(boolean on) throws IOException, ReceivedMessageParseException {
        String cmd = power.apply(on ? ON : POWER_STANDBY);
        sendCommand(cmd);
        update();
    }

    @Override
    public void setMute(boolean on) throws IOException, ReceivedMessageParseException {
        String cmd = this.mute.apply(on ? ON : OFF);
        sendCommand(cmd);
        update();
    }

    /**
     * Sets the absolute volume in decibel.
     *
     * @param volume Absolute value in decibel ([-80,+12]).
     * @throws IOException
     */
    @Override
    public void setVolumeDB(float volume) throws IOException, ReceivedMessageParseException {
        if (volume < zoneConfig.getVolumeDbMin()) {
            volume = zoneConfig.getVolumeDbMin();
        }
        if (volume > zoneConfig.getVolumeDbMax()) {
            volume = zoneConfig.getVolumeDbMax();
        }

        // Yamaha accepts only integer values with .0 or .5 at the end only (-20.5dB, -20.0dB) - at least on RX-S601D.
        // The order matters here. We want to cast to integer first and then scale by 10.
        // Effectively we're only allowing dB values with .0 at the end.
        int vol = (int) volume * 10;
        sendCommand(this.volume.apply(vol));
        update();
    }

    /**
     * Sets the volume in percent
     *
     * @param volume
     * @throws IOException
     */
    @Override
    public void setVolume(float volume) throws IOException, ReceivedMessageParseException {
        if (volume < 0) {
            volume = 0;
        }
        if (volume > 100) {
            volume = 100;
        }
        // Compute value in db
        setVolumeDB(zoneConfig.getVolumeDb(volume));
    }

    /**
     * Increase or decrease the volume by the given percentage.
     *
     * @param percent
     * @throws IOException
     */
    @Override
    public void setVolumeRelative(ZoneControlState state, float percent)
            throws IOException, ReceivedMessageParseException {
        setVolume(zoneConfig.getVolumePercentage(state.volumeDB) + percent);
    }

    @Override
    public void setInput(String name) throws IOException, ReceivedMessageParseException {
        name = inputConverterSupplier.get().toCommandName(name);
        String cmd = inputSel.apply(name);
        sendCommand(cmd);
        update();
    }

    @Override
    public void setSurroundProgram(String name) throws IOException, ReceivedMessageParseException {
        String cmd = name.equalsIgnoreCase(SURROUND_PROGRAM_STRAIGHT) ? surroundSelStraight.apply()
                : surroundSelProgram.apply(name);

        sendCommand(cmd);
        update();
    }

    @Override
    public void setDialogueLevel(int level) throws IOException, ReceivedMessageParseException {
        if (!dialogueLevelSupported) {
            return;
        }
        sendCommand(dialogueLevel.apply(level));
        update();
    }

    @Override
    public void setHDMI1Out(boolean on) throws IOException, ReceivedMessageParseException {
        if (!hdmi1OutSupported) {
            return;
        }
        sendCommandWithoutZone(hdmi1Out.apply(on ? ON : OFF));
        update();
    }

    @Override
    public void setHDMI2Out(boolean on) throws IOException, ReceivedMessageParseException {
        if (!hdmi2OutSupported) {
            return;
        }
        sendCommandWithoutZone(hdmi2Out.apply(on ? ON : OFF));
        update();
    }

    @Override
    public void setScene(String scene) throws IOException, ReceivedMessageParseException {
        if (!sceneSelSupported) {
            return;
        }
        sendCommand(sceneSel.apply(scene));
        update();
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        Node statusNode = getZoneResponse(comReference.get(), zone, ZONE_BASIC_STATUS_CMD, ZONE_BASIC_STATUS_PATH);

        String value;

        ZoneControlState state = new ZoneControlState();

        value = getNodeContentOrEmpty(statusNode, power.getPath());
        state.power = ON.equalsIgnoreCase(value);

        value = getNodeContentOrEmpty(statusNode, mute.getPath());
        state.mute = ON.equalsIgnoreCase(value);

        // The value comes in dB x 10, on AVR it says -30.5dB, the values comes as -305
        value = getNodeContentOrDefault(statusNode, volume.getPath(), String.valueOf(zoneConfig.getVolumeDbMin()));
        state.volumeDB = Float.parseFloat(value) * .1f; // in dB

        value = getNodeContentOrEmpty(statusNode, inputSel.getPath());
        state.inputID = inputConverterSupplier.get().fromStateName(value);
        if (state.inputID == null || state.inputID.isBlank()) {
            throw new ReceivedMessageParseException("Expected inputID. Failed to read Input/Input_Sel");
        }

        value = getNodeContentOrEmpty(statusNode, hdmi1Out.getPath());
        state.hdmi1Out = ON.equalsIgnoreCase(value);

        value = getNodeContentOrEmpty(statusNode, hdmi1Out.getPath());
        state.hdmi2Out = ON.equalsIgnoreCase(value);

        // Some receivers may use Src_Name instead?
        value = getNodeContentOrEmpty(statusNode, inputSelNamePath);
        state.inputName = value;

        value = getNodeContentOrEmpty(statusNode, surroundSelStraight.getPath());
        boolean straightOn = ON.equalsIgnoreCase(value);

        value = getNodeContentOrEmpty(statusNode, surroundSelProgram.getPath());
        // Surround is either in straight mode or sound program
        state.surroundProgram = straightOn ? SURROUND_PROGRAM_STRAIGHT : value;

        value = getNodeContentOrDefault(statusNode, dialogueLevel.getPath(), "0");
        state.dialogueLevel = Integer.parseInt(value);

        logger.debug("Zone {} state - power: {}, mute: {}, volumeDB: {}, input: {}, surroundProgram: {}", getZone(),
                state.power, state.mute, state.volumeDB, state.inputID, state.surroundProgram);

        observer.zoneStateChanged(state);
    }
}
