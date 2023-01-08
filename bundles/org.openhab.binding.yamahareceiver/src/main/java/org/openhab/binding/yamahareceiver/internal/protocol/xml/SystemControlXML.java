/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Models.RX_A2000;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConstants.*;
import static org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLProtocolService.getResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * The system control protocol class is used to control basic non-zone functionality
 * of a Yamaha receiver with HTTP/xml.
 * No state will be saved in here, but in {@link SystemControlState} instead.
 *
 * @author David Gräff - Initial contribution
 * @author Tomasz Maruszak - refactoring, HTR-xxxx Zone_2 compatibility
 */
public class SystemControlXML implements SystemControl {

    private final Logger logger = LoggerFactory.getLogger(SystemControlXML.class);

    private static final Set<String> MODELS_WITH_PARTY_SUPPORT = new HashSet<>(Arrays.asList(RX_A2000));

    private WeakReference<AbstractConnection> comReference;
    private SystemControlStateListener observer;
    private final DeviceDescriptorXML descriptorXML;

    protected CommandTemplate power = new CommandTemplate(
            "<System><Power_Control><Power>%s</Power></Power_Control></System>", "System/Power_Control/Power");
    protected CommandTemplate partyMode = new CommandTemplate(
            "<System><Party_Mode><Mode>%s</Mode></Party_Mode></System>", "System/Party_Mode/Mode");
    protected boolean partyModeSupported;
    protected CommandTemplate partyModeMute = new CommandTemplate(
            "<System><Party_Mode><Volume><Mute>%s</Mute></Volume></Party_Mode></System>");
    protected boolean partyModeMuteSupported;
    protected CommandTemplate partyModeVolume = new CommandTemplate(
            "<System><Party_Mode><Volume><Lvl>%s</Lvl></Volume></Party_Mode></System>");
    protected boolean partyModeVolumeSupported;

    public SystemControlXML(AbstractConnection xml, SystemControlStateListener observer,
            DeviceInformationState deviceInformationState) {
        this.comReference = new WeakReference<>(xml);
        this.observer = observer;
        this.descriptorXML = DeviceDescriptorXML.getAttached(deviceInformationState);

        this.applyModelVariations();
    }

    /**
     * Apply command changes to ensure compatibility with all supported models
     */
    protected void applyModelVariations() {
        if (descriptorXML == null) {
            logger.trace("Device descriptor not available");
            return;
        }

        logger.trace("Compatibility detection");

        partyModeSupported = descriptorXML.hasFeature(
                d -> MODELS_WITH_PARTY_SUPPORT.contains(d.getUnitName())
                        || d.system.hasCommandEnding("System,Party_Mode,Mode"),
                () -> logger.debug("The {} channel is not supported on your model", CHANNEL_PARTY_MODE));

        partyModeMuteSupported = descriptorXML.hasFeature(
                d -> MODELS_WITH_PARTY_SUPPORT.contains(d.getUnitName())
                        || d.system.hasCommandEnding("System,Party_Mode,Volume,Mute"),
                () -> logger.debug("The {} channel is not supported on your model", CHANNEL_PARTY_MODE_MUTE));

        partyModeVolumeSupported = descriptorXML.hasFeature(
                d -> MODELS_WITH_PARTY_SUPPORT.contains(d.getUnitName())
                        || d.system.hasCommandEnding("System,Party_Mode,Volume,Lvl"),
                () -> logger.debug("The {} channel is not supported on your model", CHANNEL_PARTY_MODE_VOLUME));
    }

    @Override
    public void setPower(boolean power) throws IOException, ReceivedMessageParseException {
        String cmd = this.power.apply(power ? ON : POWER_STANDBY);
        comReference.get().send(cmd);
        update();
    }

    @Override
    public void setPartyMode(boolean on) throws IOException, ReceivedMessageParseException {
        if (!partyModeSupported) {
            return;
        }
        String cmd = this.partyMode.apply(on ? ON : OFF);
        comReference.get().send(cmd);
        update();
    }

    @Override
    public void setPartyModeMute(boolean on) throws IOException, ReceivedMessageParseException {
        if (!partyModeMuteSupported) {
            return;
        }
        String cmd = this.partyModeMute.apply(on ? ON : OFF);
        comReference.get().send(cmd);
        update();
    }

    @Override
    public void setPartyModeVolume(boolean increment) throws IOException, ReceivedMessageParseException {
        if (!partyModeVolumeSupported) {
            return;
        }
        String cmd = this.partyModeVolume.apply(increment ? UP : DOWN);
        comReference.get().send(cmd);
        update();
    }

    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        if (observer == null) {
            return;
        }

        AbstractConnection conn = comReference.get();

        SystemControlState state = new SystemControlState();

        Node node = getResponse(conn, power.apply(GET_PARAM), power.getPath());
        state.power = node != null && ON.equals(node.getTextContent());

        if (partyModeSupported) {
            // prevent an unnecessary call
            node = getResponse(conn, partyMode.apply(GET_PARAM), partyMode.getPath());
            state.partyMode = node != null && ON.equals(node.getTextContent());
        }

        logger.debug("System state - power: {}, partyMode: {}", state.power, state.partyMode);

        observer.systemControlStateChanged(state);
    }
}
