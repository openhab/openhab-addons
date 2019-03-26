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
package org.openhab.binding.mcp23017.internal.handler;

import static org.openhab.binding.mcp23017.internal.Mcp23017BindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mcp23017.internal.GPIODataHolder;
import org.openhab.binding.mcp23017.internal.PinMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
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
 * The {@link Mcp23017Handler} is base class for MCP23017 chip support
 *
 * @author Anatol Ogorek - Initial contribution
 */
public class Mcp23017Handler extends BaseThingHandler implements GpioPinListenerDigital {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MCP23017GpioProvider mcpProvider;
    private Integer address;
    private Integer busNumber;
    private Mcp23017PinStateHolder pinStateHolder;
    /**
     * the polling interval mcp23071 check interrupt register (optional, defaults to 50ms)
     */
    private static final int POLLING_INTERVAL = 50;

    public Mcp23017Handler(Thing thing) {
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
            mcpProvider = initializeMcpProvider();
            pinStateHolder = new Mcp23017PinStateHolder(mcpProvider, this.thing);
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

    private MCP23017GpioProvider initializeMcpProvider() {
        MCP23017GpioProvider mcp = null;
        logger.debug("initializing mcp provider for busNumber {} and address {}", busNumber, address);
        try {
            mcp = new MCP23017GpioProvider(busNumber, address);
            mcp.setPollingTime(POLLING_INTERVAL);
        } catch (UnsupportedBusNumberException | IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Tried to access not available I2C bus: " + ex.getMessage());
        }
        logger.debug("got mcpProvider {}", mcp);
        return mcp;
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
        logger.debug("initializing pin {}, pullMode {}, mcpProvider {}", pin, pullMode, mcpProvider);
        GpioPinDigitalInput input = GPIODataHolder.GPIO.provisionDigitalInputPin(mcpProvider, pin,
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
