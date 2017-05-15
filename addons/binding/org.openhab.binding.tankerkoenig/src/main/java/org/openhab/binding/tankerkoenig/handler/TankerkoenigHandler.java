/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.handler;

import static org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TankerkoenigHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Dollinger/Jürgen Baginski
 */
public class TankerkoenigHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(TankerkoenigHandler.class);

    private String apiKey;
    private boolean setupMode;
    private boolean use_OpeningTime;
    private String locationID;
    private OpeningTimes openingTimes;

    private ScheduledFuture<?> pollingJob;

    public TankerkoenigHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no code needed.
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Tankerkoenig handler '{}'", getThing().getUID());
        Configuration config = getThing().getConfiguration();
        this.setLocationID((String) config.get(TankerkoenigBindingConstants.CONFIG_LOCATION_ID));
        this.setApiKey((String) config.get(TankerkoenigBindingConstants.CONFIG_API_KEY));
        Bridge b = this.getBridge();
        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Could not find bridge (tankerkoenig config). Did you select one?");
            return;
        }
        BridgeHandler handler = (BridgeHandler) b.getHandler();
        this.setApiKey(handler.getApiKey());
        this.setSetupMode(handler.isSetupMode());
        this.setUseOpeningTime(handler.isUseOpeningTime());
        boolean registeredSuccessfully = handler.registerTankstelleThing(getThing());
        if (!registeredSuccessfully) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The limitation of tankstellen things for one tankstellen config (the bridge) is limited to 10");
            return;
        }
        updateStatus(ThingStatus.ONLINE);

        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isUseOpeningTime()) {
                        logger.debug("Try to refresh detail data");
                        updateDetailData();
                    }
                } catch (Throwable t) {
                    logger.error("Caught exception in ScheduledExecutorService of TankerkoenigHandler. StackTrace: {}",
                            t.getStackTrace().toString());
                }

            }
        }, 15, 24 * 60 * 60, TimeUnit.SECONDS);
        logger.debug("Refresh job scheduled to run every 24 houres for '{}'", getThing().getUID());
    }

    @Override
    public void dispose() {
        this.pollingJob.cancel(true);
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        Bridge b = this.getBridge();
        BridgeHandler handler = (BridgeHandler) b.getHandler();
        handler.unregisterTankstelleThing(getThing());
        super.handleRemoval();
    }

    /***
     * Updates the channels of a tankstelle item
     *
     * @param station
     */
    public void updateData(LittleStation station) {
        logger.debug("Update Tankerkoenig data '{}'", getThing().getUID());

        DecimalType diesel = new DecimalType(station.getDiesel());
        DecimalType e10 = new DecimalType(station.getE10());
        DecimalType e5 = new DecimalType(station.getE5());

        updateState(CHANNEL_DIESEL, diesel);
        updateState(CHANNEL_E10, e10);
        updateState(CHANNEL_E5, e5);

    }

    /***
     * Updates the detail-data from tankerkoenig api, actually only the opening times are used.
     */
    public void updateDetailData() {
        logger.debug("Running UpdateTankstellenDetails");
        TankerkoenigDetailService service = new TankerkoenigDetailService();
        this.setOpeningTimes(service.getTankstellenDetailData(this.getApiKey(), locationID));
        logger.debug("UpdateTankstellenDetails openingTimes: {}", this.openingTimes);
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean setupMode) {
        this.setupMode = setupMode;
    }

    public boolean isUseOpeningTime() {
        return use_OpeningTime;
    }

    public void setUseOpeningTime(boolean use_OpeningTime) {
        this.use_OpeningTime = use_OpeningTime;
    }

    public OpeningTimes getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(OpeningTimes openingTimes) {
        this.openingTimes = openingTimes;
    }
}

