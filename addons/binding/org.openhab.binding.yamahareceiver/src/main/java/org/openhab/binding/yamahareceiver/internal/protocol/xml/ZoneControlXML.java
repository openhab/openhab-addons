/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfiguration;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.OFF;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.ON;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.POWER_STANDBY;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getNodeContentOrDefault;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLUtils.getNodeContentOrEmpty;

/**
 * The zone protocol class is used to control one zone of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link ZoneControlState} instead.
 *
 * @author David Gräff - Refactored
 * @author Eric Thill
 * @author Ben Jones
 * @author Tomasz Maruszak - Refactoring, input mapping fix, added Straight surround, volume DB fix and config improvement.
 *
 */
public class ZoneControlXML implements ZoneControl {

    protected Logger logger = LoggerFactory.getLogger(ZoneControlXML.class);

    private static final String SURROUND_PROGRAM_STRAIGHT = "Straight";

    private final ZoneControlStateListener observer;
    private final Supplier<InputConverter> inputConverterSupplier;
    private final WeakReference<AbstractConnection> comReference;
    private final Zone zone;
    private final YamahaZoneConfiguration zoneConfiguration;
    private final DeviceDescriptorXML.ZoneDescriptor zoneDescriptor;

    protected String powerCmd = "<Power_Control><Power>%s</Power></Power_Control>";
    protected String powerPath = "Power_Control/Power";
    protected String muteCmd = "<Volume><Mute>%s</Mute></Volume>";
    protected String mutePath = "Volume/Mute";
    protected String volumeCmd = "<Volume><Lvl><Val>%d</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Volume>";
    protected String volumePath = "Volume/Lvl/Val";
    protected String inputSelCmd = "<Input><Input_Sel>%s</Input_Sel></Input>";
    protected String inputSelPath = "Input/Input_Sel";
    protected String inputSelNamePath = "Input/Input_Sel_Item_Info/Title";
    protected String surroundSelProgramCmd = "<Surround><Program_Sel><Current><Sound_Program>%s</Sound_Program></Current></Program_Sel></Surround>";
    protected String surroundSelProgramPath = "Surround/Program_Sel/Current/Sound_Program";
    protected String surroundSelStraightCmd = "<Surround><Program_Sel><Current><Straight>On</Straight></Current></Program_Sel></Surround>";
    protected String surroundSelStraightPath = "Surround/Program_Sel/Current/Straight";
    protected String dialogueLevelCmd = "<Sound_Video><Dialogue_Adjust><Dialogue_Lvl>%d</Dialogue_Lvl></Dialogue_Adjust></Sound_Video>";
    protected String dialogueLevelPath = "Sound_Video/Dialogue_Adjust/Dialogue_Lvl";

    public ZoneControlXML(AbstractConnection xml,
                          Zone zone,
                          YamahaZoneConfiguration zoneSettings,
                          ZoneControlStateListener observer,
                          DeviceInformationState deviceInformationState,
                          Supplier<InputConverter> inputConverterSupplier) {

        this.comReference = new WeakReference<>(xml);
        this.zone = zone;
        this.zoneConfiguration = zoneSettings;
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

        // Most models use Volume element
        // Main_Zone,Volume,Lvl
        // Main_Zone,Volume,Mute
        // However, Yamaha RX-V3900 uses Vol instead
        // Main_Zone,Vol,Lvl
        // Main_Zone,Vol,Mute

        if (zoneDescriptor.commands.stream().anyMatch(x -> x.contains("Vol,Lvl"))) {
            volumeCmd = volumeCmd.replace("Volume", "Vol");
            volumePath = volumePath.replace("Volume", "Vol");
            logger.debug("Zone {} - adjusting command to: {}", getZone(), volumeCmd);
        }
        if (zoneDescriptor.commands.stream().anyMatch(x -> x.contains("Vol,Mute"))) {
            muteCmd = muteCmd.replace("Volume", "Vol");
            mutePath = mutePath.replace("Volume", "Vol");
            logger.debug("Zone {} - adjusting command to: {}", getZone(), muteCmd);
        }
    }

    protected void sendCommand(String message) throws IOException {
        comReference.get().send(XMLUtils.wrZone(zone, message));
    }

    /**
     * Return the zone
     */
    public Zone getZone() {
        return zone;
    }

    @Override
    public void setPower(boolean on) throws IOException, ReceivedMessageParseException {
        String cmd = String.format(powerCmd, on ? ON : POWER_STANDBY);
        sendCommand(cmd);
        update();
    }

    @Override
    public void setMute(boolean mute) throws IOException, ReceivedMessageParseException {
        String cmd = String.format(muteCmd, mute ? ON : OFF);
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
        if (volume < zoneConfiguration.getVolumeDbMin()) {
            volume = zoneConfiguration.getVolumeDbMin();
        }
        if (volume > zoneConfiguration.getVolumeDbMax()) {
            volume = zoneConfiguration.getVolumeDbMax();
        }

        // Yamaha accepts only integer values with .0 or .5 at the end only (-20.5dB, -20.0dB) - at least on RX-S601D.
        // The order matters here. We want to cast to integer first and then scale by 10.
        // Effectively we're only allowing dB values with .0 at the end.
        int vol = (int) volume * 10;
        sendCommand(String.format(volumeCmd, vol));
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
        setVolumeDB(zoneConfiguration.getVolumeDb(volume));
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
        setVolume(zoneConfiguration.getVolumePercentage(state.volumeDB) + percent);
    }

    @Override
    public void setInput(String name) throws IOException, ReceivedMessageParseException {
        name = inputConverterSupplier.get().toCommandName(name);
        String cmd = String.format(inputSelCmd, name);
        sendCommand(cmd);
        update();
    }

    @Override
    public void setSurroundProgram(String name) throws IOException, ReceivedMessageParseException {
        String cmd = name.equalsIgnoreCase(SURROUND_PROGRAM_STRAIGHT)
                ? surroundSelStraightCmd
                : String.format(surroundSelProgramCmd, name);

        sendCommand(cmd);
        update();
    }

    @Override
    public void setDialogueLevel(int level) throws IOException, ReceivedMessageParseException {
        sendCommand(String.format(dialogueLevelCmd, level));
        update();
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        Node statusNode = XMLProtocolService.getZoneResponse(comReference.get(), zone,
                "<Basic_Status>GetParam</Basic_Status>", "Basic_Status");

        String value;

        ZoneControlState state = new ZoneControlState();

        value = getNodeContentOrEmpty(statusNode, powerPath);
        state.power = ON.equalsIgnoreCase(value);

        value = getNodeContentOrEmpty(statusNode, mutePath);
        state.mute = ON.equalsIgnoreCase(value);

        // The value comes in dB x 10, on AVR it says -30.5dB, the values comes as -305
        value = getNodeContentOrDefault(statusNode, volumePath, String.valueOf(zoneConfiguration.getVolumeDbMin()));
        state.volumeDB = Float.parseFloat(value) * .1f; // in dB

        value = getNodeContentOrEmpty(statusNode, inputSelPath);
        state.inputID = inputConverterSupplier.get().fromStateName(value);
        if (StringUtils.isBlank(state.inputID)) {
            throw new ReceivedMessageParseException("Expected inputID. Failed to read Input/Input_Sel");
        }

        // Some receivers may use Src_Name instead?
        value = getNodeContentOrEmpty(statusNode, inputSelNamePath);
        state.inputName = value;

        value = getNodeContentOrEmpty(statusNode, surroundSelStraightPath);
        boolean straightOn = ON.equalsIgnoreCase(value);

        value = getNodeContentOrEmpty(statusNode, surroundSelProgramPath);
        // Surround is either in straight mode or sound program
        state.surroundProgram = straightOn ? SURROUND_PROGRAM_STRAIGHT : value;

        value = getNodeContentOrDefault(statusNode, dialogueLevelPath, "0");
        state.dialogueLevel = Integer.parseInt(value);

        logger.debug("Zone {} state - power: {}, mute: {}, volumeDB: {}, input: {}, surroundProgram: {}",
                getZone(), state.power, state.mute, state.volumeDB, state.inputID, state.surroundProgram);

        observer.zoneStateChanged(state);
    }
}
