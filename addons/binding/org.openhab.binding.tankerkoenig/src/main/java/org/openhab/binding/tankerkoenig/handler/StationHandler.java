/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigDetailResult;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Dollinger - Initial contribution
 * @author Jürgen Baginski - Initial contribution
 */
public class StationHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(StationHandler.class);

    private String apiKey;
    private boolean modeOpeningTime;
    private String locationID;
    private OpeningTimes openingTimes;
    private String userAgent;
    private final TankerkoenigService service = new TankerkoenigService();
    private TankerkoenigDetailResult result;

    private ScheduledFuture<?> pollingJob;

    public StationHandler(Thing thing) {
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
        setLocationID((String) config.get(TankerkoenigBindingConstants.CONFIG_LOCATION_ID));
        setApiKey((String) config.get(TankerkoenigBindingConstants.CONFIG_API_KEY));
        Bridge b = this.getBridge();
        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Could not find bridge (tankerkoenig config). Did you select one?");
            return;
        }
        WebserviceHandler handler = (WebserviceHandler) b.getHandler();
        userAgent = handler.getUserAgent();
        setApiKey(handler.getApiKey());
        setModeOpeningTime(handler.isModeOpeningTime());
        if (getBridge().getThings().size() > 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The limitation of station things for one tankerkoenig webservice (the bridge) is limited to 10");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Try to refresh detail data");
                updateDetailData();
            } catch (RuntimeException r) {
                logger.debug("Caught exception in ScheduledExecutorService of TankerkoenigHandler. RuntimeExcetion: {}",
                        r);
                // no status change, since in case of error in here,
                // the old values for opening time will be continue to be used
            }
        }, 15, 86400, TimeUnit.SECONDS);// 24*60*60 = 86400, a whole day in seconds!
        logger.debug("Refresh job scheduled to run every 24 hours for '{}'", getThing().getUID());
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus(), getThing().getUID());
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, bridgeStatusInfo.getDescription());
        }
    }

    /***
     * Updates the channels of a station item
     *
     * @param station
     */
    public void updateData(LittleStation station) {
        logger.debug("Update Tankerkoenig data '{}'", getThing().getUID());
        if (StringUtils.containsOnly(station.getDiesel(), "01234567890.")) {
            DecimalType diesel = new DecimalType(station.getDiesel());
            updateState(CHANNEL_DIESEL, diesel);
        } else {
            updateState(CHANNEL_DIESEL, UnDefType.UNDEF);
        }
        if (StringUtils.containsOnly(station.getE10(), "01234567890.")) {
            DecimalType e10 = new DecimalType(station.getE10());
            updateState(CHANNEL_E10, e10);
        } else {
            updateState(CHANNEL_E10, UnDefType.UNDEF);
        }
        if (StringUtils.containsOnly(station.getE5(), "01234567890.")) {
            DecimalType e5 = new DecimalType(station.getE5());
            updateState(CHANNEL_E5, e5);
        } else {
            updateState(CHANNEL_E5, UnDefType.UNDEF);
        }
        updateState(CHANNEL_STATION_OPEN, (station.isOpen() ? OpenClosedType.OPEN : OpenClosedType.CLOSED));
        updateStatus(ThingStatus.ONLINE);
    }

    /***
     * Updates the detail-data from tankerkoenig api, actually only the opening times are used.
     */
    public void updateDetailData() {
        result = service.getStationDetailData(this.getApiKey(), locationID, userAgent);

        if (result.isOk()) {
            setOpeningTimes(result.getOpeningTimes());
            StationHandler tkh = (StationHandler) this.getThing().getHandler();
            LittleStation s = result.getLittleStation();
            if (s == null) {
                logger.debug("Station with id {}  is not updated!", tkh.getLocationID());
            } else {
                tkh.updateData(s);
            }
            updateStatus(ThingStatus.ONLINE);
            WebserviceHandler handler = (WebserviceHandler) getBridge().getHandler();
            handler.updateStatus(ThingStatus.ONLINE);
            logger.debug("updateDetailData openingTimes: {}", this.openingTimes);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, result.getMessage());
        }
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

    public boolean isModeOpeningTime() {
        return modeOpeningTime;
    }

    public void setModeOpeningTime(boolean modeOpeningTime) {
        this.modeOpeningTime = modeOpeningTime;
    }

    public OpeningTimes getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(OpeningTimes openingTimes) {
        this.openingTimes = openingTimes;
    }
}
