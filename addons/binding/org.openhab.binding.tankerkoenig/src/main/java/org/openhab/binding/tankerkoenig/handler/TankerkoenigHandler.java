/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.handler;

import static org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.tankerkoenig.internal.config.Tankerkoenig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TankerkoenigHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(TankerkoenigHandler.class);

    private String apiKey;
    private String locationID;
    private int refreshInterval;
    private Tankerkoenig tankerkoenig;
    @SuppressWarnings("unused")
    private ScheduledFuture<?> pollingJob;

    public TankerkoenigHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Tankerkoenig handler '{}'", getThing().getUID());

        Configuration config = getThing().getConfiguration();
        apiKey = (String) config.get("apikey");
        locationID = (String) config.get("locationid");
        refreshInterval = ((BigDecimal) config.get("refresh")).intValue();

        tankerkoenig = new Tankerkoenig(apiKey, locationID);

        // Check api key and location id
        String validationResult = tankerkoenig.validate();

        if (!validationResult.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, validationResult);
            return;
        }

        int pollingPeriod = refreshInterval;
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, pollingPeriod, TimeUnit.MINUTES);

        logger.debug("Refresh job scheduled to run every {} min. for '{}'", pollingPeriod, getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
    }

    private synchronized void updateData() {
        logger.debug("Update Tankerkoenig data '{}'", getThing().getUID());

        tankerkoenig.update();

        // Check api key and location id
        String validationResult = tankerkoenig.validate();

        if (!validationResult.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, validationResult);
            return;
        }

        logger.info("Updating tankerkoenig items");

        DecimalType diesel = new DecimalType(tankerkoenig.getResult().getStation().getDiesel());
        DecimalType e10 = new DecimalType(tankerkoenig.getResult().getStation().getE10());
        DecimalType e5 = new DecimalType(tankerkoenig.getResult().getStation().getE5());

        updateState(CHANNEL_DIESEL, diesel);
        updateState(CHANNEL_E10, e10);
        updateState(CHANNEL_E5, e5);

    }
}
