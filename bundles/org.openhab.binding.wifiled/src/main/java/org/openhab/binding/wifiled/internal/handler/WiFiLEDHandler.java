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
package org.openhab.binding.wifiled.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.wifiled.internal.WiFiLEDBindingConstants;
import org.openhab.binding.wifiled.internal.configuration.WiFiLEDConfig;
import org.openhab.binding.wifiled.internal.handler.AbstractWiFiLEDDriver.Driver;
import org.openhab.binding.wifiled.internal.handler.AbstractWiFiLEDDriver.Protocol;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WiFiLEDHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Osman Basha - Initial contribution
 * @author Ries van Twisk
 */
public class WiFiLEDHandler extends BaseThingHandler {

    private static final int INC_DEC_STEP = 10;

    private Logger logger = LoggerFactory.getLogger(WiFiLEDHandler.class);
    private AbstractWiFiLEDDriver driver;
    private ScheduledFuture<?> pollingJob;

    public WiFiLEDHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WiFiLED handler '{}'", getThing().getUID());

        WiFiLEDConfig config = getConfigAs(WiFiLEDConfig.class);

        int port = (config.getPort() == null) ? AbstractWiFiLEDDriver.DEFAULT_PORT : config.getPort();
        Protocol protocol = config.getProtocol() == null ? Protocol.LD382A : Protocol.valueOf(config.getProtocol());
        Driver driverName = config.getDriver() == null ? Driver.CLASSIC : Driver.valueOf(config.getDriver());

        switch (driverName) {
            case CLASSIC:
                driver = new ClassicWiFiLEDDriver(this, config.getIp(), port, protocol);
                break;

            case FADING:
                int fadeDurationInMs = config.getFadeDurationInMs() == null
                        ? FadingWiFiLEDDriver.DEFAULT_FADE_DURATION_IN_MS
                        : config.getFadeDurationInMs();
                int fadeSteps = config.getFadeSteps() == null ? FadingWiFiLEDDriver.DEFAULT_FADE_STEPS
                        : config.getFadeSteps();
                driver = new FadingWiFiLEDDriver(config.getIp(), port, protocol, fadeDurationInMs, fadeSteps);
                break;
        }

        try {
            driver.init();

            logger.debug("Found a WiFi LED device '{}'", getThing().getUID());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateStatus(ThingStatus.ONLINE);

        int pollingPeriod = (config.getPollingPeriod() == null) ? 30 : config.getPollingPeriod();
        if (pollingPeriod > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> update(), 0, pollingPeriod, TimeUnit.SECONDS);
            logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WiFiLED handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        driver.shutdown();
        driver = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for {}", command, channelUID);

        try {
            if (command == RefreshType.REFRESH) {
                update();
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_POWER)) {
                handleColorCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_COLOR)) {
                handleColorCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_WHITE)) {
                handleWhiteCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_WHITE2)) {
                handleWhite2Command(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_PROGRAM)
                    && (command instanceof StringType stringCommand)) {
                driver.setProgram(stringCommand);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_PROGRAM_SPEED)) {
                handleProgramSpeedCommand(command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handleColorCommand(Command command) throws IOException {
        if (command instanceof HSBType hsbCommand) {
            driver.setColor(hsbCommand);
        } else if (command instanceof PercentType percentCommand) {
            driver.setBrightness(percentCommand);
        } else if (command instanceof OnOffType onOffCommand) {
            driver.setPower(onOffCommand);
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseType) {
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incBrightness(INC_DEC_STEP);
            } else {
                driver.decBrightness(INC_DEC_STEP);
            }
        }
    }

    private void handleWhiteCommand(Command command) throws IOException {
        if (command instanceof PercentType percentCommand) {
            driver.setWhite(percentCommand);
        } else if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setWhite(PercentType.HUNDRED);
            } else {
                driver.setWhite(PercentType.ZERO);
            }
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            if (increaseDecreaseCommand.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incWhite(INC_DEC_STEP);
            } else {
                driver.decWhite(INC_DEC_STEP);
            }
        }
    }

    private void handleWhite2Command(Command command) throws IOException {
        if (command instanceof PercentType percentCommand) {
            driver.setWhite2(percentCommand);
        } else if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setWhite2(PercentType.HUNDRED);
            } else {
                driver.setWhite2(PercentType.ZERO);
            }
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            if (increaseDecreaseCommand.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incWhite2(INC_DEC_STEP);
            } else {
                driver.decWhite2(INC_DEC_STEP);
            }
        }
    }

    private void handleProgramSpeedCommand(Command command) throws IOException {
        if (command instanceof PercentType percentCommand) {
            driver.setProgramSpeed(percentCommand);
        } else if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setProgramSpeed(PercentType.HUNDRED);
            } else {
                driver.setProgramSpeed(PercentType.ZERO);
            }
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            if (increaseDecreaseCommand.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incProgramSpeed(INC_DEC_STEP);
            } else {
                driver.decProgramSpeed(INC_DEC_STEP);
            }
        }
    }

    private synchronized void update() {
        logger.debug("Updating WiFiLED data '{}'", getThing().getUID());

        try {
            LEDStateDTO ledState = driver.getLEDStateDTO();
            HSBType color = ledState.getHSB();
            updateState(WiFiLEDBindingConstants.CHANNEL_POWER, ledState.getPower());
            updateState(WiFiLEDBindingConstants.CHANNEL_COLOR, color);
            updateState(WiFiLEDBindingConstants.CHANNEL_WHITE, ledState.getWhite());
            updateState(WiFiLEDBindingConstants.CHANNEL_WHITE2, ledState.getWhite2());
            updateState(WiFiLEDBindingConstants.CHANNEL_PROGRAM, ledState.getProgram());
            updateState(WiFiLEDBindingConstants.CHANNEL_PROGRAM_SPEED, ledState.getProgramSpeed());

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void reportCommunicationError(IOException e) {
        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
    }
}
