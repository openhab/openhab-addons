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
package org.openhab.binding.infokeydinrail.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.infokeydinrail.internal.GPIODataHolder;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinAnalogInput;

/**
 * The {@link InfokeyAnalogPinStateHolder} is a class where Infokey PIN state is held
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
public class InfokeyAnalogPinStateHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<ChannelUID, GpioPinAnalogInput> inputPins = new HashMap<>();
    private Thing thing;

    public InfokeyAnalogPinStateHolder(Thing thing) {
        this.thing = thing;
    }

    public GpioPin getInputPin(ChannelUID channel) {
        return inputPins.get(channel);
    }

    public ChannelUID getChannelUIDFromInputPin(GpioPinAnalogInput pin) {
        for (Map.Entry<ChannelUID, GpioPinAnalogInput> entry : inputPins.entrySet()) {
            if (entry.getValue().getPin().equals(pin.getPin())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void unBindGpioPins() {
        inputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
        inputPins.clear();
    }

    // public ChannelUID getChannelForInputPin(GpioPinDigitalInput pin) {
    // Optional<Entry<ChannelUID, GpioPinDigitalInput>> result = inputPins.entrySet().stream()
    // .filter(entry -> entry.getValue().equals(pin)).findFirst();
    // logger.debug("getChannelForInputPin {} {}", pin, result.isPresent());
    // if (result.isPresent()) {
    // return result.get().getKey();
    // }
    // return null;
    // }

    public void addInputPin(GpioPinAnalogInput pin, ChannelUID channel) {
        inputPins.put(channel, pin);
    }
}
