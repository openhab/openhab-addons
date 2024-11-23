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
package org.openhab.binding.onewire.internal;

import static org.openhab.binding.onewire.internal.OwBindingConstants.CHANNEL_DIGITAL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.owserver.OwserverDeviceParameter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * The {@link DigitalIoConfig} class provides the configuration of a digital IO channel
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DigitalIoConfig {
    private final String channelID;
    private final ChannelUID channelUID;
    private final OwserverDeviceParameter inParam;
    private final OwserverDeviceParameter outParam;
    private DigitalIoMode ioMode = DigitalIoMode.INPUT;
    private DigitalIoLogic ioLogic = DigitalIoLogic.NORMAL;

    public DigitalIoConfig(Thing thing, Integer channelIndex, OwserverDeviceParameter inParam,
            OwserverDeviceParameter outParam) {
        this.channelUID = new ChannelUID(thing.getUID(), String.format("%s%d", CHANNEL_DIGITAL, channelIndex));
        this.channelID = String.format("%s%d", CHANNEL_DIGITAL, channelIndex);
        this.inParam = inParam;
        this.outParam = outParam;
    }

    public void setIoMode(String ioMode) {
        this.ioMode = DigitalIoMode.valueOf(ioMode.toUpperCase());
    }

    public void setIoLogic(String ioLogic) {
        this.ioLogic = DigitalIoLogic.valueOf(ioLogic.toUpperCase());
    }

    public Boolean isInverted() {
        return (ioLogic == DigitalIoLogic.INVERTED);
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public String getChannelId() {
        return channelID;
    }

    public OwserverDeviceParameter getParameter() {
        return (ioMode == DigitalIoMode.INPUT) ? inParam : outParam;
    }

    public Boolean isInput() {
        return (ioMode == DigitalIoMode.INPUT);
    }

    public Boolean isOutput() {
        return (ioMode == DigitalIoMode.OUTPUT);
    }

    public DigitalIoMode getIoDirection() {
        return ioMode;
    }

    public State convertState(Boolean rawValue) {
        if (ioLogic == DigitalIoLogic.NORMAL) {
            return OnOffType.from(rawValue);
        } else {
            return OnOffType.from(!rawValue);
        }
    }

    public DecimalType convertState(OnOffType command) {
        if (ioLogic == DigitalIoLogic.NORMAL) {
            return command.equals(OnOffType.ON) ? new DecimalType(1) : DecimalType.ZERO;
        } else {
            return command.equals(OnOffType.ON) ? DecimalType.ZERO : new DecimalType(1);
        }
    }

    @Override
    public String toString() {
        return String.format("path=%s, mode=%s, logic=%s", getParameter(), ioMode, ioLogic);
    }
}
