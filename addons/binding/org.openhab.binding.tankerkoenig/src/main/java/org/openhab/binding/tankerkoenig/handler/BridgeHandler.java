/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.handler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.openhab.binding.tankerkoenig.TankerkoenigBindingConstants;
import org.openhab.binding.tankerkoenig.internal.config.LittleStation;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTime;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.data.TankerkoenigService;
import org.openhab.binding.tankerkoenig.internal.utility.Holiday;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHandler} is responsible for handling the things (Tankstellen)
 *
 *
 * @author Dennis Dollinger/Jürgen Baginski
 */
public class BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String apiKey;
    private int refreshInterval;
    private boolean setupMode;
    private boolean useOpeningTime;

    private List<Thing> tankstellenThingList;
    private Map<String, LittleStation> tankstellenList;

    private TankerkoenigListResult tankerkoenigListResult;

    private ScheduledFuture<?> pollingJob;

    public BridgeHandler(Bridge bridge) {
        super(bridge);
        tankstellenThingList = new ArrayList<Thing>();
        tankstellenList = new HashMap<String, LittleStation>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no code needed.
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Bridge");
        Configuration config = getThing().getConfiguration();
        setApiKey((String) config.get(TankerkoenigBindingConstants.CONFIG_API_KEY));
        setRefreshInterval(((BigDecimal) config.get(TankerkoenigBindingConstants.CONFIG_REFRESH)).intValue());
        setSetupMode((boolean) config.get(TankerkoenigBindingConstants.CONFIG_SETUP_MODE));
        setUseOpeningTime((boolean) config.get(TankerkoenigBindingConstants.CONFIG_USE_OPENINGTIME));

        updateStatus(ThingStatus.UNKNOWN);

        int pollingPeriod = this.getRefreshInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Try to refresh data");
                // Just update data if setupMode is false(off)
                try {
                    if (!isSetupMode()) {
                        updateTankstellenData();
                        updateTankstellenThings();
                    }
                } catch (RuntimeException r) {
                    logger.error("Caught exception in ScheduledExecutorService of BridgeHandler. RuntimeException: {}",
                            r);
                }
            }
        }, 30, pollingPeriod * 60, TimeUnit.SECONDS);
        logger.debug("Refresh job scheduled to run every {} min. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    /***
     *
     * @param tankstelle
     * @return
     */
    public boolean registerTankstelleThing(Thing tankstelle) {
        if (tankstellenThingList.size() >= 10) {
            return false;
        }
        logger.info("Tankstelle {} was registered to config {} ", tankstelle.getUID().toString(),
                getThing().getUID().toString());
        tankstellenThingList.add(tankstelle);
        return true;
    }

    public void unregisterTankstelleThing(Thing tankstelle) {
        logger.info("Tankstelle {} was unregistered from config {} ", tankstelle.getUID().toString(),
                getThing().getUID().toString());
        tankstellenThingList.remove(tankstelle);
    }

    /***
     * Updates the data from tankerkoenig api (no update on things)
     */
    public void updateTankstellenData() {
        // Get data
        try {
            String locationIDsString = "";
            if (useOpeningTime) {
                logger.debug("Opening times are used");
                locationIDsString = generateOpenLocationIDsString();
            } else {
                logger.debug("No opening times are used");
                locationIDsString = generateLocationIDsString();
            }
            if (locationIDsString.isEmpty()) {
                logger.info("No tankstellen id's found. Nothing to update");
                return;
            }
            TankerkoenigService service = new TankerkoenigService();
            TankerkoenigListResult result = service.getTankstellenListData(this.getApiKey(), locationIDsString);
            if (!result.isOk()) {
                // if the result is not OK, no updates are done and the status of the Bridge goes to unknown!
                updateStatus(ThingStatus.UNKNOWN);
            } else {
                updateStatus(ThingStatus.ONLINE);
                setTankerkoenigListResult(result);
                tankstellenList.clear();
                for (LittleStation station : result.getPrices().getStations()) {
                    tankstellenList.put(station.getID(), station);
                }
                logger.debug("UpdateTankstellenData: tankstellenList.size {}", tankstellenList.size());
            }
        } catch (ParseException e) {
            logger.error("ParseException: {}", e.toString());
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    /***
     * Updates all registered Tankstellen with new data
     */
    public void updateTankstellenThings() {
        logger.debug("UpdateTankstellenThings: tankstellenThingList.size {}", tankstellenThingList.size());
        for (Thing thing : tankstellenThingList) {
            TankerkoenigHandler tkh = (TankerkoenigHandler) thing.getHandler();
            LittleStation s = this.tankstellenList.get(tkh.getLocationID());
            if (s == null) {
                logger.debug("Tankstelle with id {}  is not updated, because it is not open!", tkh.getLocationID());
            } else {
                tkh.updateData(s);
            }
        }
    }

    /***
     * Generates a comma separated string with all tankstellen id's
     *
     * @return
     */
    private String generateLocationIDsString() {
        StringBuilder sb = new StringBuilder();
        for (Thing thing : tankstellenThingList) {
            TankerkoenigHandler tkh = (TankerkoenigHandler) thing.getHandler();
            if (sb.toString().isEmpty()) {
                sb.append(tkh.getLocationID());
            } else {
                sb.append(",");
                sb.append(tkh.getLocationID());
            }
        }

        return sb.toString();
    }

    /***
     * Generates a comma separated string of all open tankstellen id's
     * calculated using the data stored in opentimesList
     * The settings in the section "override" from the json detail response are NOT used!
     *
     * @return String
     * @throws ParseException
     */
    private String generateOpenLocationIDsString() throws ParseException {
        StringBuilder sb = new StringBuilder();
        DateTime now = new LocalDate().toDateTimeAtCurrentTime();
        for (Thing thing : tankstellenThingList) {
            String start = "00:00";
            String ende = "00:00";
            TankerkoenigHandler tkh = (TankerkoenigHandler) thing.getHandler();
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
                        int weekday = now.getDayOfWeek();
                        // if today is an official German holiday (Feiertag), weekday is set to Sunday!
                        if (Holiday.isHoliday(now)) {
                            weekday = 7;
                        }
                        logger.debug("Checking day: {}", day);
                        logger.debug("Todays weekday: {}", weekday);
                        // if Daily, further checking not needed!
                        if (day.contains("täglich")) {
                            logger.debug("Found a setting for daily opening times.");
                            foundIt = true;
                        } else {
                            switch (weekday) {
                                case 1:
                                    if ((day.contains("Werktags")) || (day.contains("Mo"))) {
                                        logger.debug("Found a setting which is valid for today (Monday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 2:
                                    if ((day.contains("Werktags")) || (day.contains("Di")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Tuesday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 3:
                                    if ((day.contains("Werktags")) || (day.contains("Mi")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Wednesday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 4:
                                    if ((day.contains("Werktags")) || (day.contains("Do")) || (day.contains("Mo-Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Thursday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 5:
                                    if ((day.contains("Werktags")) || (day.contains("Fr"))) {
                                        logger.debug("Found a setting which is valid for today (Fryday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 6:
                                    if ((day.contains("Wochendende")) || (day.contains("Sa"))) {
                                        logger.debug("Found a setting which is valid for today (Saturday).");
                                        foundIt = true;
                                    }
                                    break;
                                case 7:
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
            LocalTime tempopening = LocalTime.parse(start);
            LocalTime tempclosing = LocalTime.parse(ende);
            DateTime opening = tempopening.toDateTimeToday();
            DateTime closing = tempclosing.toDateTimeToday();
            if (opening.isEqual(closing)) {
                closing = opening.plusDays(1);
            } else {
                // Tankerkoenig.de does update the status "open" every 4 minutes
                // due to this the status "open" could be sent up to 4 minutes after the published opening time
                // therefore the first update is called 4 minutes after opening time!
                opening = opening.plusMinutes(4);
            }
            if ((now.isAfter(opening) & (now.isBefore(closing)))) {
                // logger.debug("Opening: {}, Closing: {}, now: {}", opening, closing, now);
                logger.debug("Now is within opening times for today.");
                if (sb.toString().equals("")) {
                    sb.append(tkh.getLocationID());
                } else {
                    sb.append("," + tkh.getLocationID());
                }
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

    public boolean isSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean setupMode) {
        this.setupMode = setupMode;
    }

    public boolean isUseOpeningTime() {
        return useOpeningTime;
    }

    public void setUseOpeningTime(boolean useOpeningTime) {
        this.useOpeningTime = useOpeningTime;
    }
}
