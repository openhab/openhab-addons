/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sensibosky.internal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sensibosky.internal.http.SensiboAcStateResponse;
import org.openhab.binding.sensibosky.internal.model.AcState;
import org.openhab.binding.sensibosky.internal.model.SensiboMeasurements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SensiboSkyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Kaczmarczyk - Initial contribution
 */
@NonNullByDefault
public class SensiboSkyHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SensiboSkyHandler.class);

    @Nullable
    private SensiboSkyConfiguration config;

    ScheduledFuture<?> refreshJob;

    public SensiboSkyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SensiboApiConnection connection = new SensiboApiConnection(config);

        SensiboAcStateResponse response = connection.readAcState();
        AcState currentState = response.result.get(0).acState;

        if (response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        if (SensiboSkyBindingConstants.HUMIDITY.equals(channelUID.getId())
                || SensiboSkyBindingConstants.CURRENT_TEMPERATURE.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                SensiboMeasurements measurements = connection.getCurrentTemperatureAndHumidity();
                State temperature = new QuantityType<>(new Float(measurements.result.measurements.temperature),
                        SIUnits.CELSIUS);
                State humidity = new QuantityType<>(new Float(measurements.result.measurements.humidity),
                        SmartHomeUnits.ONE);
                updateState(SensiboSkyBindingConstants.CURRENT_TEMPERATURE, temperature);
                updateState(SensiboSkyBindingConstants.HUMIDITY, humidity);

            }
        } else if (SensiboSkyBindingConstants.TARGET_TEMPERATURE.equals(channelUID.getId())) {
            currentState.targetTemperature = Integer.parseInt(command.toString().split(" ")[0]);
        } else if (SensiboSkyBindingConstants.FAN_MODE.equals(channelUID.getId())) {
            currentState.fanLevel = command.toString();
        } else if (SensiboSkyBindingConstants.AC_MODE.equals(channelUID.getId())) {
            currentState.mode = command.toString();
        } else if (SensiboSkyBindingConstants.SWING_MODE.equals(channelUID.getId())) {
            if (command.toString().contentEquals("ON")) {
                currentState.swing = "rangeFull";
            } else {
                currentState.swing = "stopped";
            }
        } else if (SensiboSkyBindingConstants.POWER.equals(channelUID.getId())) {
            currentState.on = command.toString().equals("ON");
        }
        if (!(command instanceof RefreshType)) {
            connection.setAcState(currentState);
        }
    }

    private void refresh(int refreshInterval) {
        Runnable runnable = () -> {
            SensiboApiConnection connection = new SensiboApiConnection(config);
            boolean thingReachable = !connection.getDeviceId().equals("");
            if (thingReachable) {
                logger.info("Sensibo Sky measurement refresh");
                SensiboMeasurements measurements = connection.getCurrentTemperatureAndHumidity();
                State temperature = new QuantityType<>(new Float(measurements.result.measurements.temperature),
                        SIUnits.CELSIUS);
                State humidity = new QuantityType<>(new Float(measurements.result.measurements.humidity),
                        SmartHomeUnits.ONE);
                updateState(SensiboSkyBindingConstants.CURRENT_TEMPERATURE, temperature);
                updateState(SensiboSkyBindingConstants.HUMIDITY, humidity);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        };

        refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(SensiboSkyConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        refresh(config.refreshInterval);
        logger.debug("Finished initializing!");
    }
}
