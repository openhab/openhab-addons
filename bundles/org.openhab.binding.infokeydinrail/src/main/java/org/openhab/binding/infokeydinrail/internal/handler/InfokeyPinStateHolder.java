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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InfokeyPinStateHolder} is a class where Infokey PIN state is held
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
public class InfokeyPinStateHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<ChannelUID, Integer> inputPins = new HashMap<>();
    private Map<ChannelUID, Integer> outputPins = new HashMap<>();
    private Thing thing;

    public InfokeyPinStateHolder(Thing thing) {
        this.thing = thing;
    }

    public Integer getInputPin(ChannelUID channel) {
        return inputPins.get(channel);
    }

    public ChannelUID getChannelFromPin(Integer pin) {
        for (Entry<ChannelUID, Integer> entry : inputPins.entrySet()) {
            if (entry.getValue().equals(pin)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public List<Integer> getInputPins() {
        return new ArrayList<Integer>(inputPins.values());
    }

    public Integer getInputSize() {
        return inputPins.size();
    }

    public Integer getOutputSize() {
        return outputPins.size();
    }

    public ChannelUID getChannelUIDFromInputPin(Integer pin) {
        for (Map.Entry<ChannelUID, Integer> entry : inputPins.entrySet()) {
            if (entry.getValue().equals(pin)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Integer getOutputPin(ChannelUID channel) {
        logger.debug("Getting output pin for channel {}", channel);
        Integer outputPin = outputPins.get(channel);
        if (outputPin == null) {
            // outputPin = initializeOutputPin(channel);
            // outputPins.put(channel, outputPin);
        }
        return outputPin;
    }

    // private GpioPinDigitalOutput initializeOutputPin(ChannelUID channel) {
    // Pin pin = PinMapper.get(channel.getIdWithoutGroup());
    // PinState pinState = PinState.LOW;
    //
    // logger.debug("initializeOutputPin for channel {}", channel);
    // Configuration configuration = thing.getChannel(channel.getId()).getConfiguration();
    //
    // if (channel != null && !channel.getAsString().toUpperCase().contains("PULSE")) {
    // if (((String) configuration.get(DEFAULT_STATE)).equalsIgnoreCase("HIGH")) {
    // pinState = PinState.HIGH;
    // }
    // }
    //
    // logger.debug("initializing for pinState {}", pinState);
    ////
    //// GpioPinDigitalOutput gpioPin = GPIODataHolder.GPIO.provisionDigitalOutputPin(mcpProvider, pin,
    //// channel.getIdWithoutGroup(), pinState);
    //
    // logger.debug("Bound digital output for PIN: {}, channel: {}, pinState: {}", pin, channel, pinState);
    // return gpioPin;
    // }

    // public void unBindGpioPins() {
    // inputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
    // inputPins.clear();
    //
    // // outputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
    // // outputPins.clear();
    // }

    // public ChannelUID getChannelForInputPin(GpioPinDigitalInput pin) {
    // Optional<Entry<ChannelUID, GpioPinDigitalInput>> result = inputPins.entrySet().stream()
    // .filter(entry -> entry.getValue().equals(pin)).findFirst();
    // logger.debug("getChannelForInputPin {} {}", pin, result.isPresent());
    // if (result.isPresent()) {
    // return result.get().getKey();
    // }
    // return null;
    // }

    public void addInputPin(Integer pin, ChannelUID channel) {
        inputPins.put(channel, pin);
    }
}
