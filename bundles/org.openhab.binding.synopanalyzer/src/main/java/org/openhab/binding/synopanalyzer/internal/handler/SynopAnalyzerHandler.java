/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyzer.internal.handler;

import static org.openhab.binding.synopanalyzer.internal.SynopAnalyzerBindingConstants.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.synopanalyser.internal.synop.Constants;
import org.openhab.binding.synopanalyser.internal.synop.Synop;
import org.openhab.binding.synopanalyser.internal.synop.SynopLand;
import org.openhab.binding.synopanalyser.internal.synop.SynopMobileLand;
import org.openhab.binding.synopanalyser.internal.synop.SynopShip;
import org.openhab.binding.synopanalyzer.internal.config.SynopAnalyzerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SynopAnalyzerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Mark Herwege - Correction for timezone treatment
 */
@NonNullByDefault
public class SynopAnalyzerHandler extends BaseThingHandler {

    private static final String OGIMET_SYNOP_PATH = "http://www.ogimet.com/cgi-bin/getsynop?block=";
    private static final int REQUEST_TIMEOUT = 5000;
    private static final DateTimeFormatter SYNOP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH00");
    private static final double KASTEN_POWER = 3.4;
    private static final double OCTA_MAX = 8.0;

    private final Logger logger = LoggerFactory.getLogger(SynopAnalyzerHandler.class);

    private @NonNullByDefault({}) ScheduledFuture<?> executionJob;
    private @NonNullByDefault({}) SynopAnalyzerConfiguration configuration;

    public SynopAnalyzerHandler(Thing thing) {
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

    private Optional<Synop> getLastAvailableSynop() {
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

                    return Optional.of(createSynopObject(synopMessage));
                }
                logger.warn("Message does not belong to station {} : {}", configuration.stationId, message);
            }
            logger.warn("No valid Synop found for last 24h");
        } catch (IOException e) {
            logger.warn("Synop request timedout : {}", e.getMessage());
        }
        return Optional.empty();
    }

    private void updateSynopChannels() {
        logger.debug("Updating device channels");

        Optional<Synop> synop = getLastAvailableSynop();
        updateStatus(synop.isPresent() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        synop.ifPresent(theSynop -> {
            getThing().getChannels().forEach(channel -> {
                String channelId = channel.getUID().getId();
                updateState(channelId, getChannelState(channelId, theSynop));
            });
        });
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
                String overcast = synop.getOvercast();
                return overcast != null ? new StringType(synop.getOvercast()) : UnDefType.NULL;
            case PRESSURE:
                return new QuantityType<>(synop.getPressure(), PRESSURE_UNIT);
            case TEMPERATURE:
                return new QuantityType<>(synop.getTemperature(), TEMPERATURE_UNIT);
            case WIND_ANGLE:
                return getWindAngle(synop);
            case WIND_DIRECTION:
                QuantityType<Angle> angle = getWindAngle(synop);
                return new StringType(getWindDirection(angle.intValue()));
            case WIND_STRENGTH:
                return getWindStrength(synop);
            case WIND_SPEED_BEAUFORT:
                QuantityType<Speed> windStrength = getWindStrength(synop);
                QuantityType<Speed> wsKpH = windStrength.toUnit(SIUnits.KILOMETRE_PER_HOUR);
                return (wsKpH != null) ? new DecimalType(Math.round(Math.pow(wsKpH.floatValue() / 3.01, 0.666666666)))
                        : UnDefType.NULL;
            case TIME_UTC:
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                int year = synop.getYear() == 0 ? now.getYear() : synop.getYear();
                int month = synop.getMonth() == 0 ? now.getMonth().getValue() : synop.getMonth();
                ZonedDateTime zdt = ZonedDateTime.of(year, month, synop.getDay(), synop.getHour(), 0, 0, 0,
                        ZoneOffset.UTC);
                return new DateTimeType(zdt);
            default:
                logger.error("Unsupported channel Id '{}'", channelId);
                return UnDefType.UNDEF;
        }
    }

    /**
     * Returns the wind direction based on degree.
     */
    private String getWindDirection(int degree) {
        double step = 360.0 / WIND_DIRECTIONS.length;
        double b = Math.floor((degree + (step / 2.0)) / step);
        return WIND_DIRECTIONS[(int) (b % WIND_DIRECTIONS.length)];
    }

    private QuantityType<Angle> getWindAngle(Synop synop) {
        return new QuantityType<>(synop.getWindDirection(), WIND_DIRECTION_UNIT);
    }

    /**
     * Returns the wind strength depending upon the unit of the message.
     */
    private QuantityType<Speed> getWindStrength(Synop synop) {
        return new QuantityType<>(synop.getWindSpeed(),
                Constants.WS_MPS.equalsIgnoreCase(synop.getWindUnit()) ? WIND_SPEED_UNIT_MS : WIND_SPEED_UNIT_KNOT);
    }

    private Synop createSynopObject(String synopMessage) {
        List<String> list = new ArrayList<>(Arrays.asList(synopMessage.split("\\s+")));
        if (synopMessage.startsWith(Constants.LAND_STATION_CODE)) {
            return new SynopLand(list);
        } else if (synopMessage.startsWith(Constants.SHIP_STATION_CODE)) {
            return new SynopShip(list);
        } else {
            return new SynopMobileLand(list);
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
