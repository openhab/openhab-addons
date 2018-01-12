/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.handler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTime;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigService;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebserviceHandler} is responsible for handling the things (stations)
 *
 *
 * @author Dennis Dollinger
 * @author Jürgen Baginski
 */
public class WebserviceHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String apiKey;
    private int refreshInterval;
    private boolean modeOpeningTime;
    private String userAgent;
    private boolean isHoliday;
    private final TankerkoenigService service = new TankerkoenigService();

    private Map<String, LittleStation> stationMap;

    private TankerkoenigListResult tankerkoenigListResult;

    private ScheduledFuture<?> pollingJob;

    public WebserviceHandler(Bridge bridge) {
        super(bridge);
        stationMap = new HashMap<String, LittleStation>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(TankerkoenigBindingConstants.CHANNEL_HOLIDAY)) {
            logger.debug("HandleCommand recieved: {}", channelUID.getId());
            isHoliday = (command == OnOffType.ON);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Bridge");
        Configuration config = getThing().getConfiguration();
        setApiKey((String) config.get(TankerkoenigBindingConstants.CONFIG_API_KEY));
        setRefreshInterval(((BigDecimal) config.get(TankerkoenigBindingConstants.CONFIG_REFRESH)).intValue());
        setModeOpeningTime((boolean) config.get(TankerkoenigBindingConstants.CONFIG_MODE_OPENINGTIME));
        // set the UserAgent, this string is used by TankerkoenigService
        // to set a custom UserAgent for the WebRequest as specifically requested by Tankerkoening.de!
        StringBuilder sb = new StringBuilder();
        sb.append("openHAB, Tankerkoenig-Binding Version ");
        Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
        sb.append(version.toString());
        userAgent = sb.toString();

        updateStatus(ThingStatus.UNKNOWN);

        int pollingPeriod = this.getRefreshInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Try to refresh data");
                try {
                    updateStationData();
                    updateStationThings();
                } catch (RuntimeException r) {
                    logger.debug("Caught exception in ScheduledExecutorService of BridgeHandler. RuntimeException: ",
                            r);
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }, pollingPeriod, pollingPeriod, TimeUnit.MINUTES);
        logger.debug("Refresh job scheduled to run every {} min. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    @Override
    public void updateStatus(ThingStatus status) {
        updateStatus(status, ThingStatusDetail.NONE, null);
    }

    /***
     * Updates the data from tankerkoenig api (no update on things)
     */
    public void updateStationData() {
        // Get data
        try {
            String locationIDsString = "";
            if (modeOpeningTime) {
                logger.debug("Opening times are used");
                locationIDsString = generateOpenLocationIDsString();
            } else {
                logger.debug("No opening times are used");
                locationIDsString = generateLocationIDsString();
            }
            if (locationIDsString.isEmpty()) {
                logger.debug("No tankstellen id's found. Nothing to update");
                return;
            }
            TankerkoenigListResult result = service.getStationListData(this.getApiKey(), locationIDsString, userAgent);
            if (!result.isOk()) {
                // two possibel reasons for result.isOK=false
                // A-tankerkoenig returns false on a web-request
                // in this case the field "message" holds information for the reason.
                // B-the web-request does not return a valid json-string,
                // in this case an emptyReturn object is created with the message "No valid response from the
                // web-request!"
                // in both cases the Webservice and the Station(s) will go OFFLINE
                // only in case A the pollingJob gets canceled!
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, result.getMessage());
                // if the Bridge goes OFFLINE, all connected Stations will go OFFLINE as well.
                // The bridge reports its statusUpdate and the things react using the bridgeStatusChanged-Method!
                // Only if the message is NOT "No valid response from the web-request!" the scheduled job gets stopped!
                if (!result.getMessage().equals(TankerkoenigBindingConstants.NO_VALID_RESPONSE)) {
                    pollingJob.cancel(true);
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                setTankerkoenigListResult(result);
                stationMap.clear();
                for (LittleStation station : result.getPrices().getStations()) {
                    station.setOpen("open".equals(station.getStatus()));
                    stationMap.put(station.getID(), station);
                }
                logger.debug("UpdateStationData: tankstellenList.size {}", stationMap.size());
            }
        } catch (ParseException e) {
            logger.error("ParseException: ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /***
     * Updates all registered station with new data
     */
    public void updateStationThings() {
        logger.debug("UpdateStationThings: getThing().getThings().size {}", getThing().getThings().size());
        for (Thing thing : getThing().getThings()) {
            StationHandler tkh = (StationHandler) thing.getHandler();
            LittleStation s = this.stationMap.get(tkh.getLocationID());
            if (s == null) {
                logger.debug("Station with id {}  is not updated!", tkh.getLocationID());
            } else {
                tkh.updateData(s);
            }
        }
    }

    /***
     * Generates a comma separated string with all station id's
     *
     * @return
     */
    private String generateLocationIDsString() {
        StringBuilder sb = new StringBuilder();
        for (Thing thing : getThing().getThings()) {
            StationHandler tkh = (StationHandler) thing.getHandler();
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(tkh.getLocationID());
        }
        return sb.toString();
    }

    /***
     * Generates a comma separated string of all open station id's
     * calculated using the data stored in opentimesList
     * The settings in the section "override" from the json detail response are NOT used!
     *
     * @return String
     * @throws ParseException
     */
    private String generateOpenLocationIDsString() throws ParseException {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();
        for (Thing thing : getThing().getThings()) {
            String start = "00:00";
            String ende = "00:00";
            StationHandler tkh = (StationHandler) thing.getHandler();
            Boolean foundIt = false;
            OpeningTimes oTimes = tkh.getOpeningTimes();
            // oTimes could be NULL, assume wholeDay open in this case!
            if (oTimes != null) {
                if (oTimes.getWholeDay()) {
                    // WholeDay open, use this ID!
                    foundIt = true;
                    logger.debug("Found a setting for WholeDay.");
                    // "start" and "ende" are set manually!
                    start = "00:00";
                    ende = "23:59";
                } else {
                    OpeningTime[] o = oTimes.getOpeningTimes();
                    logger.debug("o.length: {}", o.length);
                    int i = 0;
                    do {
                        logger.debug("Checking opening time i: {}", i);
                        String day = o[i].getText();
                        String open = o[i].getStart();
                        String close = o[i].getEnd();
                        DayOfWeek weekday = today.getDayOfWeek();
                        logger.debug("Checking day: {}", day);
                        logger.debug("Todays weekday: {}", weekday);
                        if (isHoliday) {
                            weekday = DayOfWeek.SUNDAY;
                            logger.debug("Today is a holiday using : {}", weekday);
                        }
                        // if Daily, further checking not needed!
                        if (day.contains("täglich")) {
                            logger.debug("Found a setting for daily opening times.");
                            foundIt = true;
                        } else {
                            switch (weekday) {
                                case MONDAY:
                                    if ((day.contains("Werktags")) || (day.contains("Mo"))) {
                                        logger.debug("Found a setting which is valid for today (Monday).");
                                        foundIt = true;
                                    }
                                    break;
                                case TUESDAY:
                                    if ((day.contains("Werktags")) || (day.contains("Di")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Tuesday).");
                                        foundIt = true;
                                    }
                                    break;
                                case WEDNESDAY:
                                    if ((day.contains("Werktags")) || (day.contains("Mi")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Wednesday).");
                                        foundIt = true;
                                    }
                                    break;
                                case THURSDAY:
                                    if ((day.contains("Werktags")) || (day.contains("Do")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Thursday).");
                                        foundIt = true;
                                    }
                                    break;
                                case FRIDAY:
                                    if ((day.contains("Werktags")) || (day.contains("Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Fryday).");
                                        foundIt = true;
                                    }
                                    break;
                                case SATURDAY:
                                    if ((day.contains("Wochendende")) || (day.contains("Sa"))) {
                                        logger.debug("Found a setting which is valid for today (Saturday).");
                                        foundIt = true;
                                    }
                                    break;
                                case SUNDAY:
                                    if ((day.contains("Wochenende")) || (day.contains("So"))) {
                                        logger.debug("Found a setting which is valid for today (Sunday).");
                                        foundIt = true;
                                    }
                                    break;
                            }
                            if (foundIt) {
                                start = open;
                                ende = close;
                                break;
                            }
                        }
                        i = i + 1;
                    } while (i < o.length);
                }
            } else {
                // no OpeningTimes found, assuming WholeDay open!
                foundIt = true;
                logger.debug("No OpeningTimes are found, assuming WholeDay.");
                // "start" and "ende" are set manually!
                start = "00:00";
                ende = "23:59";
            }
            LocalTime opening = LocalTime.parse(start);
            LocalTime closing = LocalTime.parse(ende);
            LocalTime now = LocalTime.now();
            if (!opening.equals(closing)) {
                // Tankerkoenig.de does update the status "open" every 4 minutes
                // due to this the status "open" could be sent up to 4 minutes after the published opening time
                // therefore the first update is called 4 minutes after opening time!
                opening = opening.plusMinutes(4);
            }
            if ((opening.equals(closing)) || ((now.isAfter(opening) & (now.isBefore(closing))))) {
                logger.debug("Now is within opening times for today.");
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(tkh.getLocationID());
            }
        }
        return sb.toString();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public TankerkoenigListResult getTankerkoenigListResult() {
        return tankerkoenigListResult;
    }

    public void setTankerkoenigListResult(TankerkoenigListResult tankerkoenigListResult) {
        this.tankerkoenigListResult = tankerkoenigListResult;
    }

    public boolean isModeOpeningTime() {
        return modeOpeningTime;
    }

    public void setModeOpeningTime(boolean modeOpeningTime) {
        this.modeOpeningTime = modeOpeningTime;
    }

    public String getUserAgent() {
        return userAgent;
    }

}
