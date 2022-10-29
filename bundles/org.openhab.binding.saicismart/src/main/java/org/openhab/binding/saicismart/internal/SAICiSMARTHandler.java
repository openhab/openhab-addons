/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.saicismart.internal;

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_FORCE_REFRESH;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_LAST_ACTIVITY;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SAICiSMARTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class SAICiSMARTHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SAICiSMARTHandler.class);

    @Nullable
    SAICiSMARTVehicleConfiguration config;
    private @Nullable Future<?> pollingJob;

    // if the binding is initialized, treat the car as active to get some first data
    private ZonedDateTime lastCarActivity = ZonedDateTime.now();

    public SAICiSMARTHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported yet
        if (channelUID.getId().equals(SAICiSMARTBindingConstants.CHANNEL_FORCE_REFRESH) && command == OnOffType.ON) {
            // reset channel to off
            updateState(CHANNEL_FORCE_REFRESH, OnOffType.from(false));
            // update internal activity date, to query the car for about a minute
            notifyCarActivity(ZonedDateTime.now().minus(9, ChronoUnit.MINUTES), true);
        } else if (channelUID.getId().equals(CHANNEL_LAST_ACTIVITY) && command instanceof DateTimeType) {
            // update internal activity date from external date
            notifyCarActivity(((DateTimeType) command).getZonedDateTime(), true);
        }
    }

    protected @Nullable SAICiSMARTBridgeHandler getBridgeHandler() {
        return (SAICiSMARTBridgeHandler) super.getBridge().getHandler();
    }

    @Override
    public void initialize() {
        config = getConfigAs(SAICiSMARTVehicleConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        // just started, make sure we start querying
        notifyCarActivity(ZonedDateTime.now(), true);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (lastCarActivity.isAfter(ZonedDateTime.now().minus(10, ChronoUnit.MINUTES))) {

                if (this.getBridgeHandler().getUid() != null && this.getBridgeHandler().getToken() != null) {
                    try {
                        boolean chargeOrRun = new VehicleStateUpdater(this).call();
                        if (chargeOrRun) {
                            // get precise data
                            new ChargeStateUpdater(this).call();
                            notifyCarActivity(ZonedDateTime.now(), true);
                        }
                    } catch (Exception e) {
                        logger.error("Could not refresh car data for {}", config.vin, e);
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void notifyCarActivity(ZonedDateTime now, boolean force) {
        // if the car activity changed, notify the channel
        if (force || lastCarActivity.isBefore(now)) {
            lastCarActivity = now;
            updateState(CHANNEL_LAST_ACTIVITY, new DateTimeType(lastCarActivity));
        }
    }

    @Override
    public void dispose() {
        Future<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }
}
