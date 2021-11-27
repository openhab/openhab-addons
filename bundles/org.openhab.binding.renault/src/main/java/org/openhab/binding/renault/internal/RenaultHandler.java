/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal;

import static org.openhab.binding.renault.internal.RenaultBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;
import static org.openhab.core.library.unit.SIUnits.METRE;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.renault.internal.renault.api.Car;
import org.openhab.binding.renault.internal.renault.api.MyRenaultHttpSession;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link RenaultHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RenaultHandler.class);

    private RenaultConfiguration config = new RenaultConfiguration();

    private @Nullable ScheduledFuture<?> pollingJob;

    private HttpClient httpClient;

    public RenaultHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // This binding only polls status data automatically.
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        this.config = getConfigAs(RenaultConfiguration.class);

        // Validate configuration
        if (this.config.myRenaultUsername.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MyRenault Username is empty!");
            return;
        }
        if (this.config.myRenaultPassword.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "MyRenault Password is empty!");
            return;
        }
        if (this.config.locale.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Location is empty!");
            return;
        }
        if (this.config.vin.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "VIN is empty!");
            return;
        }
        if (this.config.refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The refresh interval mush to be larger than 1");
            return;
        }

        // Background initialization:
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(this::getStatus, 0, config.refreshInterval, TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    private void getStatus() {
        MyRenaultHttpSession httpSession = new MyRenaultHttpSession(this.config, httpClient);
        try {
            httpSession.initSesssion();
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            httpSession = null;
            logger.error("Error My Renault Http Session.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        if (httpSession != null) {
            httpSession.updateCarData();
            updateState(httpSession.getCar());
        }
    }

    private void updateState(Car car) {
        if (car.batteryLevel != null) {
            updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(car.batteryLevel));
        }
        if (car.hvacstatus != null) {
            updateState(CHANNEL_HVAC_STATUS, OnOffType.from(car.hvacstatus));
        }
        if (car.imageURL != null) {
            updateState(CHANNEL_IMAGE, new StringType(car.imageURL));
        }
        if (car.gpsLatitude != null && car.gpsLongitude != null) {
            updateState(CHANNEL_LOCATION,
                    new PointType(new DecimalType(car.gpsLatitude), new DecimalType(car.gpsLongitude)));
        }
        if (car.odometer != null) {
            updateState(CHANNEL_ODOMETER, new QuantityType<Length>(car.odometer, KILO(METRE)));
        }
    }
}
