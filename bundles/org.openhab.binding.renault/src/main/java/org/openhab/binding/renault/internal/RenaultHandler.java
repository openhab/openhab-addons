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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.renault.internal.renault.api.MyRenaultHttpSession;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link RenaultHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RenaultHandler.class);

    private @Nullable RenaultConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    public RenaultHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (CHANNEL_BATTERY_LEVEL.equals(channelUID.getId())) {
        if (command instanceof RefreshType) {
            getStatus();
        }
        // }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        this.config = getConfigAs(RenaultConfiguration.class);

        // Background initialization:
        scheduler.execute(() -> {
            getStatus();
        });
        pollingJob = scheduler.scheduleWithFixedDelay(this::getStatus, config.refreshInterval, config.refreshInterval,
                TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private void getStatus() {
        MyRenaultHttpSession httpSession;
        try {
            httpSession = new MyRenaultHttpSession(this.config);
            updateStatus(ThingStatus.ONLINE);
            httpSession.updateCarData(this.config);

            updateState(CHANNEL_BATTERY_LEVEL, new DecimalType(httpSession.getCar().batteryLevel));
            updateState(CHANNEL_HVAC_STATUS, (httpSession.getCar().hvacstatus ? OnOffType.ON : OnOffType.OFF));
            updateState(CHANNEL_ODOMETER, new DecimalType(httpSession.getCar().odometer));
            updateState(CHANNEL_IMAGE, new StringType(httpSession.getCar().imageURL));

        } catch (Exception e) {
            httpSession = null;
            logger.error("Error initializing session.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
