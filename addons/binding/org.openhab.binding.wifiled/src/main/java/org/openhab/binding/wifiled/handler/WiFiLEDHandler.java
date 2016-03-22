/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wifiled.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wifiled.WiFiLEDBindingConstants;
import org.openhab.binding.wifiled.configuration.WiFiLEDConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WiFiLEDHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Osman Basha - Initial contribution
 */
public class WiFiLEDHandler extends BaseThingHandler {

    private static final int INC_DEC_STEP = 10;

    private Logger logger = LoggerFactory.getLogger(WiFiLEDHandler.class);
    private WiFiLEDDriver driver;
    private ScheduledFuture<?> pollingJob;

    public WiFiLEDHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WiFiLED handler '{}'", getThing().getUID());

        WiFiLEDConfig config = getConfigAs(WiFiLEDConfig.class);

        int port = (config.getPort() == null) ? WiFiLEDDriver.DEFAULT_PORT : config.getPort();
        driver = new WiFiLEDDriver(config.getIp(), port, config.getProtocol());
        try {
            driver.getLEDState();

            logger.debug("Found a WiFi LED device '{}'", getThing().getUID());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateStatus(ThingStatus.ONLINE);

        int pollingPeriod = (config.getPollingPeriod() == null) ? 30 : config.getPollingPeriod();
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());
    }


    @Override
    public void dispose() {
        logger.debug("Disposing WiFiLED handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        driver = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for {}", command, channelUID);

        try {
            if (command == RefreshType.REFRESH) {
                update();
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_COLOR)) {
                handleColorCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_WHITE)) {
                handleWhiteCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_PROGRAM)
                    && (command instanceof StringType)) {
                driver.setProgram((StringType) command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_PROGRAM_SPEED)) {
                handleProgramSpeedCommand(command);
            } else if (channelUID.getId().equals(WiFiLEDBindingConstants.CHANNEL_POWER)) {
                handlePowerCommand(command);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handlePowerCommand(Command command) throws IOException {
        if (command instanceof OnOffType) {
            OnOffType onOffCommand = (OnOffType) command;
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setOn();
            } else {
                driver.setOff();
            }
        }
    }

    private void handleColorCommand(Command command) throws IOException {
        if (command instanceof HSBType) {
            driver.setColor((HSBType) command);
        } else if (command instanceof PercentType) {
            driver.setBrightness((PercentType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incBrightness(INC_DEC_STEP);
            } else {
                driver.decBrightness(INC_DEC_STEP);
            }
        }
    }

    private void handleWhiteCommand(Command command) throws IOException {
        if (command instanceof PercentType) {
            driver.setWhite((PercentType) command);
        } else if (command instanceof OnOffType) {
            OnOffType onOffCommand = (OnOffType) command;
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setWhite(PercentType.HUNDRED);
            } else {
                driver.setWhite(PercentType.ZERO);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incWhite(INC_DEC_STEP);
            } else {
                driver.decWhite(INC_DEC_STEP);
            }
        }
    }

    private void handleProgramSpeedCommand(Command command) throws IOException {
        if (command instanceof PercentType) {
            driver.setProgramSpeed((PercentType) command);
        } else if (command instanceof OnOffType) {
            OnOffType onOffCommand = (OnOffType) command;
            if (onOffCommand.equals(OnOffType.ON)) {
                driver.setProgramSpeed(PercentType.HUNDRED);
            } else {
                driver.setProgramSpeed(PercentType.ZERO);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType increaseDecreaseType = (IncreaseDecreaseType) command;
            if (increaseDecreaseType.equals(IncreaseDecreaseType.INCREASE)) {
                driver.incProgramSpeed(INC_DEC_STEP);
            } else {
                driver.decProgramSpeed(INC_DEC_STEP);
            }
        }
    }

    private synchronized void update() {
        logger.debug("Updating WiFiLED data '{}'", getThing().getUID());

        try {
            LEDStateDTO ledState = driver.getLEDState();
            HSBType color = new HSBType(ledState.getHue(), ledState.getSaturation(), ledState.getBrightness());
            updateState(WiFiLEDBindingConstants.CHANNEL_POWER, ledState.getPowerState());
            updateState(WiFiLEDBindingConstants.CHANNEL_COLOR, color);
            updateState(WiFiLEDBindingConstants.CHANNEL_WHITE, ledState.getWhite());
            updateState(WiFiLEDBindingConstants.CHANNEL_PROGRAM, ledState.getProgram());
            updateState(WiFiLEDBindingConstants.CHANNEL_PROGRAM_SPEED, ledState.getProgramSpeed());

            if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

}
