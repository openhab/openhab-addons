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
    private boolean use_OpeningTime;

    private ArrayList<Thing> tankstellenThingList;
    private HashMap<String, LittleStation> tankstellenList;

    TankerkoenigListResult tankerkoenigListResult;

    private ScheduledFuture<?> pollingJob;

    public BridgeHandler(Bridge bridge) {
        super(bridge);
        this.tankstellenThingList = new ArrayList<Thing>();
        tankstellenList = new HashMap<String, LittleStation>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no code needed.
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        this.setApiKey((String) config.get(TankerkoenigBindingConstants.CONFIG_API_KEY));
        this.setRefreshInterval(((BigDecimal) config.get(TankerkoenigBindingConstants.CONFIG_REFRESH)).intValue());
        this.setSetupMode((boolean) config.get(TankerkoenigBindingConstants.CONFIG_SETUP_MODE));
        this.setUseOpeningTime((boolean) config.get(TankerkoenigBindingConstants.CONFIG_USE_OPENINGTIME));

        updateStatus(ThingStatus.ONLINE);

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
                } catch (Throwable t) {
                    logger.error("Caught exception in ScheduledExecutorService of BridgeHandler. StackTrace: {}",
                            t.getStackTrace().toString());
                }
            }
        }, 30, pollingPeriod * 60, TimeUnit.SECONDS);
        logger.debug("Refresh job scheduled to run every {} min. for '{}'", pollingPeriod, getThing().getUID());
    }

    @Override
    public void dispose() {
        this.pollingJob.cancel(true);
    }

    /***
     *
     * @param tankstelle
     * @return
     */
    public boolean registerTankstelleThing(Thing tankstelle) {
        if (this.tankstellenThingList.size() == 10) {
            return false;
        }
        logger.info("Tankstelle {} was registered to config {} ", tankstelle.getUID().toString(),
                this.getThing().getUID().toString());
        this.tankstellenThingList.add(tankstelle);
        return true;
    }

    public void unregisterTankstelleThing(Thing tankstelle) {
        logger.info("Tankstelle {} was unregistered from config {} ", tankstelle.getUID().toString(),
                this.getThing().getUID().toString());
        this.tankstellenThingList.remove(tankstelle);
    }

    /***
     * Updates the data from tankerkoenig api (no update on things)
     */
    public void updateTankstellenData() {
        // Get data
        try {
            String locationIDsString = "";
            if (use_OpeningTime) {
                logger.debug("Opening times are used");
                locationIDsString = generateOpenLocationIDsString();
            } else {
                logger.debug("No opening times are used");
                locationIDsString = generateLocationIDsString();
            }
            if (locationIDsString.length() < 1) {
                logger.info("No tankstellen id's found. Nothing to update");
                return;
            }
            TankerkoenigService service = new TankerkoenigService();
            TankerkoenigListResult result = service.getTankstellenListData(this.getApiKey(), locationIDsString);
            this.setTankerkoenigListResult(result);

            this.tankstellenList.clear();
            for (LittleStation station : result.getPrices().getStations()) {
                this.tankstellenList.put(station.getID(), station);
            }
            logger.debug("UpdateTankstellenData: tankstellenList.size {}", tankstellenList.size());
        } catch (ParseException e) {
            logger.info("ParseException: {}", e.toString());
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
            if (sb.toString().equals("")) {
                sb.append(tkh.getLocationID());
            } else {
                sb.append("," + tkh.getLocationID());
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
        try {
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
                            if (isHoliday(now)) {
                                weekday = 7;
                            }
                            logger.debug("Checking day: {}", day);
                            logger.debug("Todays weekday: {}", weekday);
                            // if Daily, further checking not needed!
                            if (day.contains("t�glich")) {
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
                                        if ((day.contains("Werktags")) || (day.contains("Di"))
                                                || (day.contains("Mo-Fr"))) {
                                            logger.debug("Found a setting which is valid for today (Tuesday).");
                                            foundIt = true;
                                        }
                                        break;
                                    case 3:
                                        if ((day.contains("Werktags")) || (day.contains("Mi"))
                                                || (day.contains("Mo-Fr"))) {
                                            logger.debug("Found a setting which is valid for today (Wednesday).");
                                            foundIt = true;
                                        }
                                        break;
                                    case 4:
                                        if ((day.contains("Werktags")) || (day.contains("Do"))
                                                || (day.contains("Mo-Fr"))) {
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
                                    i = o.length;
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
        } catch (Exception e) {
            logger.debug("Exception in BridgeHandler: {}", e);
            return null;
        }
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
        return use_OpeningTime;
    }

    public void setUseOpeningTime(boolean use_OpeningTime) {
        this.use_OpeningTime = use_OpeningTime;
    }

    public boolean isHoliday(DateTime now) {
        // Checks if today is a German holiday (Feiertag im ganzen Bundesgebiet!)
        // Code from Openhab1-Addons Samples-Rules
        int year = now.getYear();
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        boolean holiday = false;
        // String holidayName = null;
        LocalDate easterSunday = LocalDate.parse(year + "-" + month + "-" + day);
        LocalDate stAdvent = LocalDate.parse(year + "-12-25")
                .minusDays(((LocalDate.parse(year + "-12-25").getDayOfWeek()) + 21));
        int dayOfYear = now.getDayOfYear();
        // bundesweiter Feiertag
        if (dayOfYear == LocalDate.parse(year + "-01-01").getDayOfYear()) {
            // holidayName = "new_years_day"; // Neujahr
            holiday = true;
        }
        // Baden-W�rttemberg, Bayern, Sachsen-Anhalt
        else if (dayOfYear == LocalDate.parse(year + "-01-06").getDayOfYear()) {
            // holidayName = "holy_trinity";// Heilige 3 K�nige
            holiday = false;
        }
        // Carnival ;-)
        else if (dayOfYear == easterSunday.getDayOfYear() - 48) {
            // holidayName = "carnival_monday"; // Rosenmontag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() - 2) {
            // holidayName = "good_friday"; // Karfreitag
            holiday = true;
        }
        // Brandenburg
        else if (dayOfYear == easterSunday.getDayOfYear()) {
            // holidayName = "easter_sunday"; // Ostersonntag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 1) {
            // holidayName = "easter_monday"; // Ostermontag
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-05-01").getDayOfYear()) {
            // holidayName = "1st_may";// Tag der Arbeit
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 39) {
            // holidayName = "ascension_day"; // Christi Himmelfahrt
            holiday = true;
        }
        // Brandenburg
        else if (dayOfYear == easterSunday.getDayOfYear() + 49) {
            // holidayName = "whit_sunday"; // Pfingstsonntag
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == easterSunday.getDayOfYear() + 50) {
            // holidayName = "whit_monday"; // Pfingstmontag
            holiday = true;
        }
        // Baden-W�rttemberg, Bayern, Hessen, NRW, Rheinland-Pfalz, Saarland sowie regional in Sachsen, Th�ringen
        else if (dayOfYear == easterSunday.getDayOfYear() + 60) {
            // holidayName = "corpus_christi"; // Frohnleichnahm
            holiday = false;
        }
        // Saarland sowie regional in Bayern
        else if (dayOfYear == LocalDate.parse(year + "-08-15").getDayOfYear()) {
            // holidayName = "assumption_day"; // Mari� Himmelfahrt
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-10-03").getDayOfYear()) {
            // holidayName = "reunification"; // Tag der deutschen Einheit
            holiday = true;
        }
        // Brandenburg, Mecklenburg-Vorpommern, Sachsen, Sachsen-Anhalt, Th�ringen
        else if (dayOfYear == LocalDate.parse(year + "-10-31").getDayOfYear()) {
            // holidayName = "reformation_day"; // Reformationstag
            holiday = false;
        }
        // Baden-W�rttemberg, Bayern, NRW, Rheinland-Pfalz, Saarland
        else if (dayOfYear == LocalDate.parse(year + "-11-01").getDayOfYear()) {
            // holidayName = "all_saints_day"; // Allerheiligen
            holiday = false;
        }
        // religi�ser Tag
        else if (dayOfYear == stAdvent.getDayOfYear() - 14) {
            // holidayName = "remembrance_day"; // Volkstrauertag
            holiday = false;
        }
        // religi�ser Tag
        else if (dayOfYear == stAdvent.getDayOfYear() - 7) {
            // holidayName = "sunday_in_commemoration_of_the_dead"; // Totensonntag
            holiday = false;
        }
        // Sachsen
        else if (dayOfYear == stAdvent.getDayOfYear() - 11) {
            // holidayName = "day_of_repentance"; // Bu�- und Bettag
            holiday = false;
        }
        // kann auch der 4te Advent sein
        else if (dayOfYear == LocalDate.parse(year + "-12-24").getDayOfYear()) {
            // holidayName = "christmas_eve"; // Heiligabend
            holiday = false;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-12-25").getDayOfYear()) {
            // holidayName = "1st_christmas_day"; // 1. Weihnachtstag
            holiday = true;
        }
        // bundesweiter Feiertag
        else if (dayOfYear == LocalDate.parse(year + "-12-26").getDayOfYear()) {
            // holidayName = "2nd_christmas_day"; // 2. Weihnachtstag
            holiday = true;
        }
        // Silvester
        else if (dayOfYear == LocalDate.parse(year + "-12-31").getDayOfYear()) {
            // holidayName = "new_years_eve"; // Silvester
            holiday = false;
        }
        return holiday;
    }
}
