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
package org.openhab.binding.tankerkoenig.internal.handler;

import static org.openhab.binding.tankerkoenig.internal.TankerkoenigBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.openhab.binding.tankerkoenig.internal.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigService;
import org.openhab.binding.tankerkoenig.internal.dto.LittleStation;
import org.openhab.binding.tankerkoenig.internal.dto.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.dto.TankerkoenigDetailResult;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
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
    private static final Pattern IS_NUMERIC_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

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
        Bridge b = getBridge();
        if (b == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Could not find bridge (tankerkoenig config). Did you select one?");
            return;
        }
        WebserviceHandler handler = (WebserviceHandler) b.getHandler();
        userAgent = handler.getUserAgent();
        setApiKey(handler.getApiKey());
        setModeOpeningTime(handler.isModeOpeningTime());
        if (b.getThings().size() > 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The limitation of station things for one tankerkoenig webservice (the bridge) is limited to 10.");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Try to refresh detail data");
                updateDetailData();
            } catch (RuntimeException r) {
                logger.debug("Caught exception in ScheduledExecutorService of TankerkoenigHandler", r);
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
        if (station.isOpen()) {
            logger.debug("Checked Station is open! '{}'", getThing().getUID());
            updateState(CHANNEL_STATION_OPEN, OpenClosedType.OPEN);
            if (station.getDiesel() != null) {
                if (IS_NUMERIC_PATTERN.matcher(station.getDiesel()).matches()) {
                    DecimalType diesel = new DecimalType(station.getDiesel());
                    updateState(CHANNEL_DIESEL, diesel);
                } else {
                    updateState(CHANNEL_DIESEL, UnDefType.UNDEF);
                }
            } else {
                updateState(CHANNEL_DIESEL, UnDefType.UNDEF);
            }
            if (station.getE10() != null) {
                if (IS_NUMERIC_PATTERN.matcher(station.getE10()).matches()) {
                    DecimalType e10 = new DecimalType(station.getE10());
                    updateState(CHANNEL_E10, e10);
                } else {
                    updateState(CHANNEL_E10, UnDefType.UNDEF);
                }
            } else {
                updateState(CHANNEL_E10, UnDefType.UNDEF);
            }
            if (station.getE10() != null) {
                if (IS_NUMERIC_PATTERN.matcher(station.getE5()).matches()) {
                    DecimalType e5 = new DecimalType(station.getE5());
                    updateState(CHANNEL_E5, e5);
                } else {
                    updateState(CHANNEL_E5, UnDefType.UNDEF);
                }
            } else {
                updateState(CHANNEL_E5, UnDefType.UNDEF);
            }
        } else {
            logger.debug("Checked Station is closed!");
            updateState(CHANNEL_STATION_OPEN, OpenClosedType.CLOSED);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /***
     * Updates the detail-data from tankerkoenig api, actually only the opening times are used.
     */
    public void updateDetailData() {
        result = service.getStationDetailData(getApiKey(), locationID, userAgent);

        if (result.isOk()) {
            setOpeningTimes(result.getOpeningTimes());
            LittleStation s = result.getLittleStation();
            if (s == null) {
                logger.debug("Station with id {} is not updated!", getLocationID());
            } else {
                updateData(s);
            }
            updateStatus(ThingStatus.ONLINE);
            WebserviceHandler handler = (WebserviceHandler) getBridge().getHandler();
            handler.updateStatus(ThingStatus.ONLINE);
            logger.debug("updateDetailData openingTimes: {}", openingTimes);
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
