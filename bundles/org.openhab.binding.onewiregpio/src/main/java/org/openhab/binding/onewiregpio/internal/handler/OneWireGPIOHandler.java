/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.onewiregpio.internal.handler;

import static org.openhab.binding.onewiregpio.internal.OneWireGPIOBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openhab.binding.onewiregpio.internal.OneWireGPIOBindingConstants;
import org.openhab.binding.onewiregpio.internal.OneWireGpioConfiguration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
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
 * The {@link OneWireGPIOHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Anatol Ogorek - Initial contribution
 * @author Konstantin Polihronov - Changed configuration handling and added new parameter - precision
 */
public class OneWireGPIOHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OneWireGPIOHandler.class);

    private String gpioBusFile;
    private Integer refreshTime;
    private Integer precision;

    private ScheduledFuture<?> sensorRefreshJob;

    public OneWireGPIOHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(TEMPERATURE)) {
            if (command instanceof RefreshType) {
                publishSensorValue(channelUID);
            } else {
                logger.debug("Command {} is not supported for channel: {}. Supported command: REFRESH", command,
                        channelUID.getId());
            }
        }
    }

    @Override
    public void initialize() {
        OneWireGpioConfiguration configuration = getConfigAs(OneWireGpioConfiguration.class);
        gpioBusFile = configuration.gpio_bus_file;
        refreshTime = configuration.refresh_time;
        precision = configuration.precision.intValue();
        logger.debug("GPIO Busfile={}, RefreshTime={}, precision={}", gpioBusFile, refreshTime, precision);

        if (checkConfiguration()) {
            startAutomaticRefresh();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * This method checks if the provided configuration is valid.
     * When invalid parameter is found, default value is assigned.
     */
    private boolean checkConfiguration() {
        if (gpioBusFile == null || gpioBusFile.isEmpty()) {
            logger.debug("GPIO_BUS_FILE not set. Please check configuration, and set proper path to w1_slave file.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The path to the w1_slave sensor data file is missing.");
            return false;
        }

        if (refreshTime <= 0) {
            logger.debug("Refresh time [{}] is not valid. Falling back to default value: {}.", refreshTime,
                    DEFAULT_REFRESH_TIME);
            refreshTime = DEFAULT_REFRESH_TIME;
        }

        if (precision < 0 || precision > MAX_PRECISION_VALUE) {
            logger.debug(
                    "Precision value {} is outside allowed values [0 - {}]. Falling back to maximum precision value.",
                    precision, MAX_PRECISION_VALUE);
            precision = MAX_PRECISION_VALUE;
        }

        return true;
    }

    private void startAutomaticRefresh() {
        Runnable refresher = () -> {
            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (isLinked(channel.getUID().getId())) {
                    publishSensorValue(channel.getUID());
                }
            }
        };

        sensorRefreshJob = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime.intValue(), TimeUnit.SECONDS);
        logger.debug("Start automatic refresh every {} seconds", refreshTime.intValue());
    }

    private void publishSensorValue(ChannelUID channelUID) {
        String channelID = channelUID.getId();
        switch (channelID) {
            case TEMPERATURE:
                publishTemperatureSensorState(channelUID);
                break;
            default:
                logger.debug("Can not update channel with ID : {} - channel name might be wrong!", channelID);
                break;
        }
    }

    private void publishTemperatureSensorState(ChannelUID channelUID) {
        BigDecimal temp = readSensorTemperature(gpioBusFile);
        if (temp != null) {
            updateState(channelUID, new QuantityType<>(temp, SIUnits.CELSIUS));
        }
    }

    private BigDecimal readSensorTemperature(String gpioFile) {
        try (Stream<String> stream = Files.lines(Paths.get(gpioFile))) {
            Optional<String> temperatureLine = stream
                    .filter(s -> s.contains(OneWireGPIOBindingConstants.FILE_TEMP_MARKER)).findFirst();
            if (temperatureLine.isPresent()) {
                String line = temperatureLine.get();
                String tempString = line.substring(line.indexOf(OneWireGPIOBindingConstants.FILE_TEMP_MARKER)
                        + OneWireGPIOBindingConstants.FILE_TEMP_MARKER.length());
                Integer intTemp = Integer.parseInt(tempString);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
                return calculateValue(intTemp);
            } else {
                logger.debug(
                        "GPIO file didn't contain line with 't=' where temperature value should be available. Check if configuration points to the proper file");
                return null;
            }
        } catch (IOException | InvalidPathException e) {
            logger.debug("error reading GPIO bus file. File path is: {}.  Check if path is proper.", gpioFile, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error reading GPIO bus file.");
            return null;
        }
    }

    private BigDecimal calculateValue(Integer intTemp) {
        BigDecimal result = BigDecimal.valueOf(intTemp).movePointLeft(3);
        if (precision != MAX_PRECISION_VALUE) {
            result = result.setScale(precision, RoundingMode.HALF_UP);
        }
        logger.debug("Thing = {}, temperature value = {}.", getThing().getUID(), result);
        return result;
    }

    @Override
    public void dispose() {
        if (sensorRefreshJob != null) {
            sensorRefreshJob.cancel(true);
        }
        super.dispose();
    }
}
