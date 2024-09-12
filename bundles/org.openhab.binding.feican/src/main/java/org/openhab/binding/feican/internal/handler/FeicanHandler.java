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
package org.openhab.binding.feican.internal.handler;

import static org.openhab.binding.feican.internal.FeicanBindingConstants.*;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.feican.internal.Commands;
import org.openhab.binding.feican.internal.Connection;
import org.openhab.binding.feican.internal.FeicanConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FeicanHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class FeicanHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FeicanHandler.class);
    private final Commands commands = new Commands();

    private @NonNullByDefault({}) Connection connection;

    /**
     * Creates a new handler for the Feican thing.
     *
     * @param thing The thing to create the handler for
     */
    public FeicanHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof OnOffType onOffCommand) {
                handleOnOff(onOffCommand);
            } else if (command instanceof HSBType hsbCommand) {
                handleColor(channelUID, hsbCommand);
            } else if (command instanceof PercentType percentCommand) {
                handlePercentage(channelUID, percentCommand);
            } else if (command instanceof StringType stringCommand) {
                handleString(channelUID, stringCommand);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        final FeicanConfiguration config = getConfigAs(FeicanConfiguration.class);

        logger.debug("Initializing Feican Wifi RGWB Bulb on IP address {}", config.ipAddress);
        try {
            connection = new Connection(config.ipAddress);
            updateStatus(ThingStatus.ONLINE);
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (SocketException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Handle for {@link OnOffType} commands.
     *
     * @param onOff value to set: on or off
     * @throws IOException Connection to the bulb failed
     */
    private void handleOnOff(OnOffType onOff) throws IOException {
        connection.sendCommand(commands.switchOnOff(onOff));
    }

    /**
     * Handle for {@link DecimalType} as an {@link OnOffType} command.
     *
     * @param value value to derive on or off state from
     * @throws IOException Connection to the bulb failed
     */
    private void handleOnOff(DecimalType value) throws IOException {
        handleOnOff(OnOffType.from(!DecimalType.ZERO.equals(value)));
    }

    /**
     * Handle setting the color.
     *
     * The Feican bulb has a separate command for the brightness. This brightness value is applied to any color value
     * send. Because in the color command the brightness is also calculated this would mean the brightness will be
     * applied twice; first when getting the RGB values and second in the bulb itself when passing a color value a
     * earlier set brightness value is applied. To work around this, the brightness value is first send to the bulb and
     * the color is send with a 100% brightness. So the brightness is controlled by the bulb. This is also needed for 2
     * reasons. First color temperature also works with brightness and thus to set it with color temperature the
     * brightness must be set on the bulb. Secondly when setting brightness in the color widget only the brightness
     * value is passed and no color value is available, therefore this binding then sets the brightness on the bulb.
     *
     * @param channelUID Channel the command is for
     * @param command color to set
     * @throws IOException Connection to the bulb failed
     */
    private void handleColor(ChannelUID channelUID, HSBType command) throws IOException {
        if (CHANNEL_COLOR.equals(channelUID.getId())) {
            handleBrightness(command.getBrightness());
            connection.sendCommand(
                    commands.color(new HSBType(command.getHue(), command.getSaturation(), PercentType.HUNDRED)));
            handleOnOff(command);
        }
    }

    /**
     * Handle percentType commands. Action depends on what channel send the command. For brightness related channels
     * after the brightness command an extra onOff command is send to update the onOff state conform the brightness
     * state.
     *
     * @param channelUID Channel the command is for
     * @param command The percentType command
     * @throws IOException Connection to the bulb failed
     */
    private void handlePercentage(ChannelUID channelUID, PercentType command) throws IOException {
        String id = channelUID.getId();

        switch (id) {
            case CHANNEL_COLOR:
                handleBrightness(command);
                handleOnOff(command);
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                handleColorTemperature(command);
                handleOnOff(OnOffType.ON);
                break;
            case CHANNEL_PROGRAM_SPEED:
                handleProgramSpeed(command);
                handleOnOff(OnOffType.ON);
                break;
        }
    }

    /**
     * Handles the brightness command.
     *
     * @param command percentage of brightness to set
     * @throws IOException Connection to the bulb failed
     */
    private void handleBrightness(PercentType command) throws IOException {
        connection.sendCommand(commands.brightness(command));
    }

    /**
     * Handles the color temperature command.
     *
     * @param command color temperature as set a value between 0 and 100
     * @throws IOException Connection to the bulb failed
     */
    private void handleColorTemperature(PercentType command) throws IOException {
        connection.sendCommand(commands.colorTemperature(command));
    }

    /**
     * Handles the speed of a preset program.
     *
     * @param command the speed as set as a percentage value
     * @throws IOException Connection to the bulb failed
     */
    private void handleProgramSpeed(PercentType command) throws IOException {
        connection.sendCommand(commands.programSpeed(command));
    }

    /**
     * Handles setting a preset program.
     *
     * @param channelUID works for the program channel
     * @param command String value as id of the program to set
     * @throws IOException Connection to the bulb failed
     */
    private void handleString(ChannelUID channelUID, StringType command) throws NumberFormatException, IOException {
        if (CHANNEL_PROGRAM.equals(channelUID.getId())) {
            connection.sendCommand(commands.program(Integer.valueOf(command.toFullString())));
        }
    }
}
