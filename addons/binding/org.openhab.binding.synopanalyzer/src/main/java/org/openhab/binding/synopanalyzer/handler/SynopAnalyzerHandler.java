/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer.handler;

import static org.openhab.binding.synopanalyzer.SynopAnalyzerBindingConstants.*;
import static org.openhab.binding.synopanalyzer.internal.UnitUtils.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.synopanalyzer.internal.config.SynopAnalyzerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nwpi.Constants;
import com.nwpi.synop.Synop;
import com.nwpi.synop.SynopLand;
import com.nwpi.synop.SynopMobileLand;
import com.nwpi.synop.SynopShip;

/**
 * The {@link SynopAnalyzerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Mark Herwege - Correction for timezone treatment
 */

public class SynopAnalyzerHandler extends BaseThingHandler {

    private static final String OGIMET_SYNOP_PATH = "http://www.ogimet.com/cgi-bin/getsynop?block=";
    private static final int REQUEST_TIMEOUT = 5000;
    private static final DateTimeFormatter SYNOP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH00");
    private static final String MPS = "m/s";
    private static final String KNOTS = "knots";
    private static final String UTC = "UTC";
    private static final double KASTEN_POWER = 3.4;
    private static final double OCTA_MAX = 8.0;

    private Logger logger = LoggerFactory.getLogger(SynopAnalyzerHandler.class);

    private ScheduledFuture<?> executionJob;
    private SynopAnalyzerConfiguration configuration;

    public SynopAnalyzerHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SynopAnalyzerConfiguration.class);

        logger.info("Scheduling Synop update thread to run every {} minute for Station '{}'",
                configuration.refreshInterval, configuration.stationId);

        executionJob = scheduler.scheduleWithFixedDelay(() -> {
            updateSynopChannels();
        }, 0, configuration.refreshInterval, TimeUnit.MINUTES);
        updateStatus(ThingStatus.ONLINE);

    }

    private Synop getLastAvailableSynop() {
        logger.debug("Retrieving last Synop message");

        String url = forgeURL();
        try {
            String answer = HttpUtil.executeUrl("GET", url, REQUEST_TIMEOUT);
            List<String> messages = Arrays.asList(answer.split("\n"));
            if (!messages.isEmpty()) {
                String message = messages.get(messages.size() - 1);
                logger.debug(message);
                if (message.startsWith(configuration.stationId)) {
                    logger.debug("Valid Synop message received");

                    List<String> messageParts = Arrays.asList(message.split(","));
                    String synopMessage = messageParts.get(messageParts.size() - 1);

                    return createSynopObject(synopMessage);
                }
            }
            logger.warn("No valid Synop found for last 24h");
        } catch (IOException e) {
            logger.warn("Synop request timedout : {}", e.getMessage());
        }
        return null;
    }

    private void updateSynopChannels() {
        logger.debug("Updating device channels");

        Synop synop = getLastAvailableSynop();
        updateStatus(synop != null ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        if (synop != null) {
            getThing().getChannels().forEach(channel -> {
                String channelId = channel.getUID().getId();
                updateState(channelId, getChannelState(channelId, synop));
            });
        }
    }

    private State getChannelState(String channelId, Synop synop) {

        switch (channelId) {
            case HORIZONTAL_VISIBILITY:
                return new StringType(synop.getHorizontalVisibility());
            case OCTA:
                return new DecimalType(Math.max(0, synop.getOcta()));
            case ATTENUATION_FACTOR:
                double kc = Math.max(0, Math.min(synop.getOcta(), OCTA_MAX)) / OCTA_MAX;
                kc = Math.pow(kc, KASTEN_POWER);
                kc = 1 - 0.75 * kc;
                return new DecimalType(kc);
            case OVERCAST:
                return new StringType(synop.getOvercast());
            case PRESSURE:
                return new DecimalType(synop.getPressure());
            case TEMPERATURE:
                return new DecimalType(synop.getTemperature());
            case WIND_ANGLE:
                return new DecimalType(synop.getWindDirection());
            case WIND_DIRECTION:
                int angle = synop.getWindDirection();
                String direction = getWindDirection(angle);
                return new StringType(direction);
            case WIND_SPEED_MS:
                if (synop.getWindUnit().equalsIgnoreCase(MPS)) {
                    return new DecimalType(synop.getWindSpeed());
                } else {
                    Double kmhSpeed = knotsToKmh(new Double(synop.getWindSpeed()));
                    return new DecimalType(kmhToMps(kmhSpeed));
                }
            case WIND_SPEED_KNOTS:
                if (synop.getWindUnit().equalsIgnoreCase(KNOTS)) {
                    return new DecimalType(synop.getWindSpeed());
                } else {
                    Double kmhSpeed = mpsToKmh(new Double(synop.getWindSpeed()));
                    Double knotSpeed = kmhToKnots(kmhSpeed);
                    return new DecimalType(knotSpeed);
                }
            case WIND_SPEED_BEAUFORT:
                Double kmhSpeed;
                if (synop.getWindUnit().equalsIgnoreCase(MPS)) {
                    kmhSpeed = mpsToKmh(new Double(synop.getWindSpeed()));
                } else {
                    kmhSpeed = knotsToKmh(new Double(synop.getWindSpeed()));
                }
                Double beaufort = kmhToBeaufort(kmhSpeed);
                return new DecimalType(beaufort);
            case TIME_UTC:
                Calendar observationTime = Calendar.getInstance(TimeZone.getTimeZone(UTC));
                observationTime.set(Calendar.DAY_OF_MONTH, synop.getDay());
                observationTime.set(Calendar.HOUR_OF_DAY, synop.getHour());
                observationTime.set(Calendar.MINUTE, 0);
                observationTime.set(Calendar.SECOND, 0);
                observationTime.set(Calendar.MILLISECOND, 0);
                return new DateTimeType(observationTime);
            default:
                logger.error("Unsupported channel Id '{}'", channelId);
                return UnDefType.UNDEF;
        }
    }

    private Synop createSynopObject(String synopMessage) {
        ArrayList<String> liste = new ArrayList<String>(Arrays.asList(synopMessage.split("\\s+")));
        if (synopMessage.startsWith(Constants.LAND_STATION_CODE)) {
            return new SynopLand(liste);
        } else if (synopMessage.startsWith(Constants.SHIP_STATION_CODE)) {
            return new SynopShip(liste);
        } else {
            return new SynopMobileLand(liste);
        }
    }

    private String forgeURL() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        String beginDate = SYNOP_DATE_FORMAT.format(utc);

        StringBuilder url = new StringBuilder().append(OGIMET_SYNOP_PATH).append(configuration.stationId)
                .append("&begin=").append(beginDate);

        return url.toString();
    }

    @Override
    public void dispose() {
        if (executionJob != null && !executionJob.isCancelled()) {
            executionJob.cancel(true);
            executionJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateSynopChannels();
        }
    }

}
