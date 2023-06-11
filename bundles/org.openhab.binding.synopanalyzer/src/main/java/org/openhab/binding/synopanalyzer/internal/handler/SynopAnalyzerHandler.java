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
package org.openhab.binding.synopanalyzer.internal.handler;

import static org.openhab.binding.synopanalyzer.internal.SynopAnalyzerBindingConstants.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Speed;
import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.synopanalyzer.internal.config.SynopAnalyzerConfiguration;
import org.openhab.binding.synopanalyzer.internal.stationdb.Station;
import org.openhab.binding.synopanalyzer.internal.synop.Overcast;
import org.openhab.binding.synopanalyzer.internal.synop.Synop;
import org.openhab.binding.synopanalyzer.internal.synop.SynopLand;
import org.openhab.binding.synopanalyzer.internal.synop.SynopMobile;
import org.openhab.binding.synopanalyzer.internal.synop.SynopShip;
import org.openhab.binding.synopanalyzer.internal.synop.WindDirections;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    private static final String OGIMET_SYNOP_PATH = "http://www.ogimet.com/cgi-bin/getsynop?block=%s&begin=%s";
    private static final int REQUEST_TIMEOUT_MS = 5000;
    private static final DateTimeFormatter SYNOP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH00");
    private static final double KASTEN_POWER = 3.4;
    private static final double OCTA_MAX = 8.0;

    private final Logger logger = LoggerFactory.getLogger(SynopAnalyzerHandler.class);
    private final LocationProvider locationProvider;
    private final List<Station> stations;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private @NonNullByDefault({}) String formattedStationId;

    public SynopAnalyzerHandler(Thing thing, LocationProvider locationProvider, List<Station> stations) {
        super(thing);
        this.locationProvider = locationProvider;
        this.stations = stations;
    }

    @Override
    public void initialize() {
        SynopAnalyzerConfiguration configuration = getConfigAs(SynopAnalyzerConfiguration.class);
        formattedStationId = String.format("%05d", configuration.stationId);
        logger.info("Scheduling Synop update thread to run every {} minute for Station '{}'",
                configuration.refreshInterval, formattedStationId);

        if (thing.getProperties().isEmpty()) {
            discoverAttributes(configuration.stationId, locationProvider.getLocation());
        }

        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::updateChannels, 0,
                configuration.refreshInterval, TimeUnit.MINUTES));
    }

    private void discoverAttributes(int stationId, @Nullable PointType serverLocation) {
        stations.stream().filter(s -> stationId == s.idOmm).findFirst().ifPresent(station -> {
            Map<String, String> properties = new HashMap<>(
                    Map.of("Usual name", station.usualName, "Location", station.getLocation()));

            if (serverLocation != null) {
                PointType stationLocation = new PointType(station.getLocation());
                DecimalType distance = serverLocation.distanceFrom(stationLocation);

                properties.put("Distance", new QuantityType<>(distance, SIUnits.METRE).toString());
            }
            updateProperties(properties);
        });
    }

    private Optional<Synop> getLastAvailableSynop() {
        logger.debug("Retrieving last Synop message");

        String url = forgeURL();
        try {
            String answer = HttpUtil.executeUrl(HttpMethod.GET, url, REQUEST_TIMEOUT_MS);
            List<String> messages = Arrays.asList(answer.split("\n"));
            if (!messages.isEmpty()) {
                String message = messages.get(messages.size() - 1);
                logger.debug(message);
                if (message.startsWith(formattedStationId)) {
                    logger.debug("Valid Synop message received");

                    List<String> messageParts = Arrays.asList(message.split(","));
                    String synopMessage = messageParts.get(messageParts.size() - 1);

                    return createSynopObject(synopMessage);
                }
                logger.warn("Message does not belong to station {} : {}", formattedStationId, message);
            }
            logger.warn("No valid Synop found for last 24h");
        } catch (IOException e) {
            logger.warn("Synop request timedout : {}", e.getMessage());
        }
        return Optional.empty();
    }

    private void updateChannels() {
        logger.debug("Updating device channels");

        getLastAvailableSynop().ifPresentOrElse(synop -> {
            updateStatus(ThingStatus.ONLINE);
            getThing().getChannels().forEach(channel -> {
                String channelId = channel.getUID().getId();
                if (isLinked(channelId)) {
                    updateState(channelId, getChannelState(channelId, synop));
                }
            });
        }, () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No Synop message available"));
    }

    private State getChannelState(String channelId, Synop synop) {
        int octa = synop.getOcta();
        switch (channelId) {
            case HORIZONTAL_VISIBILITY:
                return new StringType(synop.getHorizontalVisibility().name());
            case OCTA:
                return octa >= 0 ? new DecimalType(octa) : UnDefType.NULL;
            case ATTENUATION_FACTOR:
                if (octa >= 0) {
                    double kc = Math.max(0, Math.min(octa, OCTA_MAX)) / OCTA_MAX;
                    kc = 1 - 0.75 * Math.pow(kc, KASTEN_POWER);
                    return new DecimalType(kc);
                }
                return UnDefType.NULL;
            case OVERCAST:
                Overcast overcast = Overcast.fromOcta(octa);
                return overcast == Overcast.UNDEFINED ? UnDefType.NULL : new StringType(overcast.name());
            case PRESSURE:
                return new QuantityType<>(synop.getPressure(), PRESSURE_UNIT);
            case TEMPERATURE:
                return new QuantityType<>(synop.getTemperature(), TEMPERATURE_UNIT);
            case WIND_ANGLE:
                return new QuantityType<>(synop.getWindDirection(), WIND_DIRECTION_UNIT);
            case WIND_DIRECTION:
                return new StringType(WindDirections.getWindDirection(synop.getWindDirection()).name());
            case WIND_STRENGTH:
                return getWindStrength(synop);
            case WIND_SPEED_BEAUFORT:
                QuantityType<Speed> wsKpH = getWindStrength(synop).toUnit(SIUnits.KILOMETRE_PER_HOUR);
                return wsKpH != null ? new DecimalType(Math.round(Math.pow(wsKpH.floatValue() / 3.01, 0.666666666)))
                        : UnDefType.NULL;
            case TIME_UTC:
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                int year = synop.getYear() == 0 ? now.getYear() : synop.getYear();
                int month = synop.getMonth() == 0 ? now.getMonth().getValue() : synop.getMonth();
                return new DateTimeType(
                        ZonedDateTime.of(year, month, synop.getDay(), synop.getHour(), 0, 0, 0, ZoneOffset.UTC));
            default:
                logger.error("Unsupported channel Id '{}'", channelId);
                return UnDefType.UNDEF;
        }
    }

    /**
     * Returns the wind strength depending upon the unit of the message.
     */
    private QuantityType<Speed> getWindStrength(Synop synop) {
        return new QuantityType<>(synop.getWindSpeed(), synop.getWindUnit());
    }

    private Optional<Synop> createSynopObject(String synopMessage) {
        List<String> list = new ArrayList<>(Arrays.asList(synopMessage.split("\\s+")));
        if (synopMessage.startsWith(LAND_STATION_CODE)) {
            return Optional.of(new SynopLand(list));
        } else if (synopMessage.startsWith(SHIP_STATION_CODE)) {
            return Optional.of(new SynopShip(list));
        } else if (synopMessage.startsWith(MOBILE_LAND_STATION_CODE)) {
            return Optional.of(new SynopMobile(list));
        }
        return Optional.empty();
    }

    private String forgeURL() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        String beginDate = SYNOP_DATE_FORMAT.format(utc);
        return String.format(OGIMET_SYNOP_PATH, formattedStationId, beginDate);
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            updateChannels();
        }
    }
}
