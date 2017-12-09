/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader.handler;

import static org.openhab.binding.logreader.LogReaderBindingConstants.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LogReaderHandler} is responsible for everything
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class LogReaderHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LogReaderHandler.class);

    @Nullable
    private ScheduledFuture<?> scheduledRefreshJob;

    boolean firstRead = true;
    static String filePath = "";
    static String logRotated = "";
    static int lastPosition;
    static int warningLines;
    static int errorLines;

    // TODO Better regex matching to get [WARN ] and [ERROR] only from specific section
    Pattern lineStartPattern = Pattern.compile("^\\d{4}.\\d{2}.\\d{2}");
    Pattern warningPattern = Pattern.compile("WARN");
    Pattern errorPattern = Pattern.compile("ERROR");

    Matcher lineStartMatcher = lineStartPattern.matcher("");
    Matcher warningMatcher = warningPattern.matcher("");
    Matcher errorMatcher = errorPattern.matcher("");

    public LogReaderHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_READER_LOGROTATED:
                case CHANNEL_READER_LASTLINE:
                case CHANNEL_READER_LASTREAD:
                case CHANNEL_READER_WARNINGS:
                case CHANNEL_READER_ERRORS:
                case CHANNEL_READER_LASTWARNING:
                case CHANNEL_READER_LASTERROR:
            }
        } else {
            logger.debug("Unsupported command {}!", command);
        }
    }

    @Override
    public void initialize() {
        try {
            logger.debug("Initializing LogReader");

            // Get config
            int refreshRate = ((Number) getConfig().get(REFRESHRATE)).intValue();
            filePath = (System.getProperty("openhab.logdir") + "/openhab.log");

            // Start scheduler
            scheduledRefreshJob = scheduler.scheduleWithFixedDelay(this::readLog, 2, refreshRate, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e1) {
            logger.debug("Exception occurred during initalization: {}", e1.getMessage(), e1);
            if (scheduledRefreshJob != null) {
                scheduledRefreshJob.cancel(true);
                scheduledRefreshJob = null;
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e1.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.info("Stopping LogReader");
        lastPosition = 0;
        if (scheduledRefreshJob != null) {
            scheduledRefreshJob.cancel(true);
            scheduledRefreshJob = null;
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    public void readLog() {

        String lastWarningLine = "";
        String lastErrorLine = "";
        String brLastLine = "";

        // On first run only count lines
        if (firstRead) {
            long lineCounterStartTime = System.currentTimeMillis();
            try (LineNumberReader lnr = new LineNumberReader(new FileReader(filePath))) {
                int lnrCounter = 0;
                String lnrLine;
                while ((lnrLine = lnr.readLine()) != null) {
                    // Get log rotation
                    if (lnrCounter == 0) {
                        lineStartMatcher.reset(lnrLine);
                        if (lineStartMatcher.find()) {
                            String[] parts = lnrLine.split(" ");
                            String joined = String.join(" ", parts[0], parts[1]);
                            logRotated = joined;
                            updateState(CHANNEL_READER_LOGROTATED, new DateTimeType(timeStamp(logRotated)));
                        } else {
                            logger.warn("First line could not be read. Timestamp mismatch: {}", lnrLine);
                        }
                    }
                    lnrCounter = lnr.getLineNumber();
                }
                lastPosition = lnrCounter;
                firstRead = false;
            } catch (Exception e2) {
                logger.debug("Exception occurred while counting lines: {}", e2.getMessage(), e2);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e2.getMessage());
            }
            long lineCounterEndTime = System.currentTimeMillis();
            logger.trace("Log lines at first read: {}", lastPosition);
            logger.trace("First log read took {} milliseconds", (lineCounterEndTime - lineCounterStartTime));
            logger.trace("Log rotated: {}", logRotated);
            return;
        }
        long readLogStartTime = System.currentTimeMillis();

        // Reset counters
        warningLines = 0;
        errorLines = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int brCounter = 0;
            String brLine;
            while ((brLine = br.readLine()) != null) {

                // Read only first line and get rotation time
                if (brCounter == 0) {
                    lineStartMatcher.reset(brLine);
                    if (lineStartMatcher.find()) {
                        String[] parts = brLine.split(" ");
                        String joined = String.join(" ", parts[0], parts[1]);

                        // Check if log has rotated since last read
                        if (!(logRotated.equals(joined))) {
                            logger.debug("Log rotation detected. Resetting line counter");
                            lastPosition = 0;
                            updateState(CHANNEL_READER_LOGROTATED, new DateTimeType(timeStamp(logRotated)));
                        }
                        logRotated = joined;
                    } else {
                        logger.warn("First line could not be read. Timestamp mismatch: {}", brLine);
                    }
                }
                // Start matching from new lines
                if (brCounter > lastPosition) {
                    warningMatcher.reset(brLine);
                    errorMatcher.reset(brLine);
                    lineStartMatcher.reset(brLine);
                    if (warningMatcher.find()) {
                        warningLines++;
                        lastWarningLine = brLine;
                    }
                    if (errorMatcher.find()) {
                        errorLines++;
                        lastErrorLine = brLine;
                    }
                    if (lineStartMatcher.find()) {
                        brLastLine = brLine;
                    }

                }
                brCounter++;
            }

            // Get last lines timestamp
            String[] parts2 = brLastLine.split(" ");
            String lastLine = String.join(" ", parts2[0], parts2[1]);

            // Debugging
            logger.debug("Last line: {}", lastLine);
            logger.debug("Total log lines: {}. New lines since last read: {}", brCounter, (brCounter - lastPosition));
            logger.debug("New warning lines: {}", warningLines);
            logger.debug("New error lines: {}", errorLines);

            lastPosition = brCounter;

            // Update channels
            updateState(CHANNEL_READER_WARNINGS, new DecimalType(warningLines));
            updateState(CHANNEL_READER_ERRORS, new DecimalType(errorLines));
            updateState(CHANNEL_READER_LASTLINE, new DateTimeType(timeStamp(lastLine)));
            if (warningLines > 0) {
                updateState(CHANNEL_READER_LASTWARNING, new StringType(lastWarningLine.replace("[WARN ]", "")));
            }
            if (errorLines > 0) {
                updateState(CHANNEL_READER_LASTERROR, new StringType(lastErrorLine.replace("[ERROR]", "")));
            }
            updateState(CHANNEL_READER_LASTREAD, new DateTimeType());
        } catch (Exception e3) {
            logger.warn("Exception occurred while parsing log: {}", e3.getMessage(), e3);
        }
        long readLogEndTime = System.currentTimeMillis();
        logger.trace("Parsing through log took {} milliseconds", (readLogEndTime - readLogStartTime));
    }

    public Calendar timeStamp(String string) {
        Calendar cal = Calendar.getInstance();

        int year = Integer.parseInt(string.substring(0, 4));
        int month = Integer.parseInt(string.substring(5, 7));
        int day = Integer.parseInt(string.substring(8, 10));
        int hour = Integer.parseInt(string.substring(11, 13));
        int min = Integer.parseInt(string.substring(14, 16));
        int sec = Integer.parseInt(string.substring(17, 19));
        int msec = Integer.parseInt(string.substring(20, 23));
        cal.set(year, month - 1, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, msec);

        return cal;
    }
}
