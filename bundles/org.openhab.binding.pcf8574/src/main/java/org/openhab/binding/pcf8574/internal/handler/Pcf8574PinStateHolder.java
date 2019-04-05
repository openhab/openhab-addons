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
package org.openhab.binding.pcf8574.internal.handler;

import static org.openhab.binding.pcf8574.internal.Pcf8574BindingConstants.DEFAULT_STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.pcf8574.internal.GPIODataHolder;
import org.openhab.binding.pcf8574.internal.PinMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.pcf.PCF8574GpioProvider;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * The {@link Pcf8574PinStateHolder} is a class where PCF8574 PIN state is held
 *
 * @author Tomasz Jagusz - Initial contribution, based on MCP23017 by Anatol Ogorek
 */
public class Pcf8574PinStateHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<ChannelUID, GpioPinDigitalInput> inputPins = new HashMap<>();
    private Map<ChannelUID, GpioPinDigitalOutput> outputPins = new HashMap<>();
    private PCF8574GpioProvider pcfProvider;
    private Thing thing;

    public Pcf8574PinStateHolder(PCF8574GpioProvider pcfProvider, Thing thing) {
        this.pcfProvider = pcfProvider;
        this.thing = thing;
    }

    public GpioPin getInputPin(ChannelUID channel) {
        return inputPins.get(channel);
    }

    public GpioPinDigitalOutput getOutputPin(ChannelUID channel) {
        logger.debug("Getting output pin for channel {}", channel);
        GpioPinDigitalOutput outputPin = outputPins.get(channel);
        if (outputPin == null) {
            outputPin = initializeOutputPin(channel);
            outputPins.put(channel, outputPin);
        }
        return outputPin;
    }

    private GpioPinDigitalOutput initializeOutputPin(ChannelUID channel) {
        Pin pin = PinMapper.get(channel.getIdWithoutGroup());
        logger.debug("initializeOutputPin for channel {}", channel);
        Configuration configuration = thing.getChannel(channel.getId()).getConfiguration();
        PinState pinState = PinState.valueOf((String) configuration.get(DEFAULT_STATE));
        logger.debug("initializing for pinState {}", pinState);
        GpioPinDigitalOutput gpioPin = GPIODataHolder.GPIO.provisionDigitalOutputPin(pcfProvider, pin,
                channel.getIdWithoutGroup(), pinState);
        logger.debug("Bound digital output for PIN: {}, channel: {}, pinState: {}", pin, channel, pinState);
        return gpioPin;
    }

    public void unBindGpioPins() {
        inputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
        inputPins.clear();

        outputPins.values().stream().forEach(gpioPin -> GPIODataHolder.GPIO.unprovisionPin(gpioPin));
        outputPins.clear();
    }

    public ChannelUID getChannelForInputPin(GpioPinDigitalInput pin) {
        Optional<Entry<ChannelUID, GpioPinDigitalInput>> result = inputPins.entrySet().stream()
                .filter(entry -> entry.getValue().equals(pin)).findFirst();
        if (result.isPresent()) {
            return result.get().getKey();
        }
        return null;
    }

    public void addInputPin(GpioPinDigitalInput pin, ChannelUID channel) {
        inputPins.put(channel, pin);
    }
}
