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

import static org.openhab.binding.pcf8574.internal.Pcf8574BindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.pcf8574.internal.GPIODataHolder;
import org.openhab.binding.pcf8574.internal.PinMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.pcf.PCF8574GpioProvider;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * The {@link Pcf8574Handler} is base class for PCF8574 chip support
 *
 * @author Tomasz Jagusz - Initial contribution, based on MCP23017 by Anatol Ogorek
 */
public class Pcf8574Handler extends BaseThingHandler implements GpioPinListenerDigital {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private PCF8574GpioProvider pcfProvider;
    private Integer address;
    private Integer busNumber;
    private Pcf8574PinStateHolder pinStateHolder;
    /**
     * the polling interval pcf8574 check interrupt register (optional, defaults to 50ms)
     */
    //private static final int POLLING_INTERVAL = 50;

    public Pcf8574Handler(Thing thing) {
        super(thing);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command: {} on channelGroup {} on channel {}", command.toFullString(),
                channelUID.getGroupId(), channelUID.getIdWithoutGroup());

        if (!verifyChannel(channelUID)) {
            return;
        }

        String channelGroup = channelUID.getGroupId();

        switch (channelGroup) {
            case CHANNEL_GROUP_INPUT:
                handleInputCommand(channelUID, command);
                break;
            case CHANNEL_GROUP_OUTPUT:
                handleOutputCommand(channelUID, command);
            default:
                break;
        }
    }

    @Override
    public void initialize() {
        try {
            checkConfiguration();
            pcfProvider = initializePcfProvider();
            pinStateHolder = new Pcf8574PinStateHolder(pcfProvider, this.thing);
            updateStatus(ThingStatus.ONLINE);
        } catch (IllegalArgumentException | SecurityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "An exception occurred while adding pin. Check pin configuration. Exception: " + e.getMessage());
        }
    }

    private boolean verifyChannel(ChannelUID channelUID) {
        if (!isChannelGroupValid(channelUID) || !isChannelValid(channelUID)) {
            logger.warn("Channel group or channel is invalid. Probably configuration problem");
            return false;
        }
        return true;
    }

    private void handleOutputCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            GpioPinDigitalOutput outputPin = pinStateHolder.getOutputPin(channelUID);
            Configuration configuration = this.getThing().getChannel(channelUID.getId()).getConfiguration();

            // invertLogic is null if not configured
            boolean activeLowFlag = ACTIVE_LOW_ENABLED.equalsIgnoreCase(configuration.get(ACTIVE_LOW).toString());
            PinState pinState = command == OnOffType.ON ^ activeLowFlag ? PinState.HIGH : PinState.LOW;
            logger.debug("got output pin {} for channel {} and command {} [active_low={}, new_state={}]", outputPin, channelUID, command, activeLowFlag, pinState);            
            GPIODataHolder.GPIO.setState(pinState, outputPin);
        }
    }

    private void handleInputCommand(ChannelUID channelUID, Command command) {
        logger.debug("Nothing to be done in handleCommand for contact.");
    }

    private boolean isChannelGroupValid(ChannelUID channelUID) {
        if (!channelUID.isInGroup()) {
            logger.debug("Defined channel not in group: {}", channelUID.getAsString());
            return false;
        }
        boolean channelGroupValid = SUPPORTED_CHANNEL_GROUPS.contains(channelUID.getGroupId());
        logger.debug("Defined channel in group: {}. Valid: {}", channelUID.getGroupId(), channelGroupValid);

        return channelGroupValid;
    }

    private boolean isChannelValid(ChannelUID channelUID) {
        boolean channelValid = SUPPORTED_CHANNELS.contains(channelUID.getIdWithoutGroup());
        logger.debug("Is channel {} in supported channels: {}", channelUID.getIdWithoutGroup(), channelValid);
        return channelValid;
    }

    protected void checkConfiguration() {
        Configuration configuration = getConfig();
        address = Integer.parseInt((configuration.get(ADDRESS)).toString(), 16);
        busNumber = Integer.parseInt((configuration.get(BUS_NUMBER)).toString());
    }

    private PCF8574GpioProvider initializePcfProvider() {
    	PCF8574GpioProvider pcf = null;
        logger.debug("initializing pcf provider for busNumber {} and address {}", busNumber, address);
        try {
            pcf = new PCF8574GpioProvider(busNumber, address);
            //pcf.setPollingTime(POLLING_INTERVAL);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("got pcfProvider {}", pcf);
        return pcf;
    }

    private GpioPinDigitalInput initializeInputPin(ChannelUID channel) {
        logger.debug("initializing input pin for channel {}", channel.getAsString());
        Pin pin = PinMapper.get(channel.getIdWithoutGroup());

        String pullMode = DEFAULT_PULL_MODE;
        if (thing.getChannel(channel.getId()) != null) {
            Configuration configuration = thing.getChannel(channel.getId()).getConfiguration();
            pullMode = ((String) configuration.get(PULL_MODE)) != null ? ((String) configuration.get(PULL_MODE))
                    : DEFAULT_PULL_MODE;
        }
        logger.debug("initializing pin {}, pullMode {}, pcfProvider {}", pin, pullMode, pcfProvider);
        GpioPinDigitalInput input = GPIODataHolder.GPIO.provisionDigitalInputPin(pcfProvider, pin,
                channel.getIdWithoutGroup(), PinPullResistance.valueOf(pullMode));
        input.addListener(this);
        logger.debug("Bound digital input for PIN: {}, ItemName: {}, pullMode: {}", pin, channel.getAsString(),
                pullMode);
        return input;
    }

    @Override
    public void dispose() {
        pinStateHolder.unBindGpioPins();
        super.dispose();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        GpioPin pin = event.getPin();
        OpenClosedType state = OpenClosedType.CLOSED;
        if (event.getState() == PinState.LOW) {
            state = OpenClosedType.OPEN;
        }
        ChannelUID channelForPin = pinStateHolder.getChannelForInputPin((GpioPinDigitalInput) pin);
        logger.debug("updating channel {} with state {}", channelForPin, state);
        updateState(channelForPin, state);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        synchronized (this) {
            logger.debug("channel linked {}", channelUID.getAsString());
            if (!verifyChannel(channelUID)) {
                return;
            }
            String channelGroup = channelUID.getGroupId();

            if (channelGroup != null && channelGroup.equals(CHANNEL_GROUP_INPUT)) {
                if (pinStateHolder.getInputPin(channelUID) != null) {
                    return;
                }
                GpioPinDigitalInput inputPin = initializeInputPin(channelUID);
                pinStateHolder.addInputPin(inputPin, channelUID);

            }
            super.channelLinked(channelUID);
        }
    }
}
