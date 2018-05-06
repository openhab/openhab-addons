/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mcp23017.handler;

import static org.openhab.binding.mcp23017.Mcp23017BindingConstants.DEFAULT_STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.mcp23017.internal.GPIODataHolder;
import org.openhab.binding.mcp23017.internal.PinMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * The {@link Mcp23017PinStateHolder} is a class where MCP23017 PIN state is held
 *
 * @author Anatol Ogorek - Initial contribution
 */
public class Mcp23017PinStateHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<ChannelUID, GpioPinDigitalInput> inputPins = new HashMap<>();
    private Map<ChannelUID, GpioPinDigitalOutput> outputPins = new HashMap<>();
    private MCP23017GpioProvider mcpProvider;
    private Thing thing;

    public Mcp23017PinStateHolder(MCP23017GpioProvider mcpProvider, Thing thing) {
        this.mcpProvider = mcpProvider;
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
        GpioPinDigitalOutput gpioPin = GPIODataHolder.GPIO.provisionDigitalOutputPin(mcpProvider, pin,
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
