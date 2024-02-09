/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.handler;

import static org.openhab.binding.airquality.internal.AirQualityBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airquality.internal.AirQualityException;
import org.openhab.binding.airquality.internal.api.ApiBridge;
import org.openhab.binding.airquality.internal.api.Appreciation;
import org.openhab.binding.airquality.internal.api.Index;
import org.openhab.binding.airquality.internal.api.Pollutant;
import org.openhab.binding.airquality.internal.api.Pollutant.SensitiveGroup;
import org.openhab.binding.airquality.internal.api.dto.AirQualityData;
import org.openhab.binding.airquality.internal.config.AirQualityConfiguration;
import org.openhab.binding.airquality.internal.config.SensitiveGroupConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirQualityStationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Łukasz Dywicki - Initial contribution
 */
@NonNullByDefault
public class AirQualityStationHandler extends BaseThingHandler {
    private final @NonNullByDefault({}) ClassLoader classLoader = AirQualityStationHandler.class.getClassLoader();
    private final Logger logger = LoggerFactory.getLogger(AirQualityStationHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> refreshJob;

    public AirQualityStationHandler(Thing thing, TimeZoneProvider timeZoneProvider, LocationProvider locationProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.locationProvider = locationProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Air Quality handler.");

        if (thing.getProperties().isEmpty()) {
            discoverAttributes();
        }

        AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);
        try {
            config.checkValid();
            freeRefreshJob();
            refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublishData, 0, config.refresh,
                    TimeUnit.MINUTES);
        } catch (AirQualityException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    private void discoverAttributes() {
        getAirQualityData().ifPresent(data -> {
            // Update thing properties
            Map<String, String> properties = new HashMap<>(Map.of(ATTRIBUTIONS, data.getAttributions()));
            PointType serverLocation = locationProvider.getLocation();
            if (serverLocation != null) {
                data.getCity().ifPresent(city -> {
                    double distance = serverLocation.distanceFrom(new PointType(city.getGeo())).doubleValue();
                    properties.put(DISTANCE, new QuantityType<>(distance / 1000, KILO(SIUnits.METRE)).toString());
                });
            }

            // Search and remove missing pollutant channels
            List<Channel> channels = new ArrayList<>(getThing().getChannels());
            Stream.of(Pollutant.values()).forEach(pollutant -> {
                String groupName = pollutant.name().toLowerCase();
                double value = data.getIaqiValue(pollutant);
                channels.removeIf(channel -> value == -1 && groupName.equals(channel.getUID().getGroupId()));
            });

            // Update thing configuration
            Configuration config = editConfiguration();
            config.put(AirQualityConfiguration.STATION_ID, data.getStationId());

            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channels).withConfiguration(config).withProperties(properties);
            data.getCity().map(city -> thingBuilder.withLocation(city.getName()));
            updateThing(thingBuilder.build());
        });
    }

    private void updateAndPublishData() {
        getAirQualityData().ifPresent(data -> {
            getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId())).forEach(channel -> {
                State state;
                ChannelUID channelUID = channel.getUID();
                ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                if (channelTypeUID != null && SENSITIVE.equals(channelTypeUID.getId().toString())) {
                    SensitiveGroupConfiguration configuration = channel.getConfiguration()
                            .as(SensitiveGroupConfiguration.class);
                    state = getSensitive(configuration.asSensitiveGroup(), data);
                } else {
                    state = getValue(channelUID.getIdWithoutGroup(), channelUID.getGroupId(), data);
                }
                updateState(channelUID, state);
            });
        });
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Air Quality handler.");
        freeRefreshJob();
    }

    private void freeRefreshJob() {
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublishData();
            return;
        }
        logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
    }

    /**
     * Request new air quality data to the aqicn.org service
     *
     * @return an optional air quality data object mapping the JSON response
     */
    private Optional<AirQualityData> getAirQualityData() {
        AirQualityData result = null;
        ApiBridge apiBridge = getApiBridge();
        if (apiBridge != null) {
            AirQualityConfiguration config = getConfigAs(AirQualityConfiguration.class);
            try {
                result = apiBridge.getData(config.stationId, config.location);
                updateStatus(ThingStatus.ONLINE);
            } catch (AirQualityException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return Optional.ofNullable(result);
    }

    private @Nullable ApiBridge getApiBridge() {
        Bridge bridge = this.getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AirQualityBridgeHandler) {
                return ((AirQualityBridgeHandler) handler).getApiBridge();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/incorrect-bridge");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        return null;
    }

    private State indexedValue(String channelId, double idx, @Nullable Pollutant pollutant) {
        Index index = Index.find(idx);
        if (index != null) {
            switch (channelId) {
                case INDEX:
                    return new DecimalType(idx);
                case VALUE:
                    return pollutant != null ? pollutant.toQuantity(idx) : UnDefType.UNDEF;
                case ICON:
                    byte[] bytes = getResource(String.format("picto/%s.svg", index.getCategory().name().toLowerCase()));
                    return bytes != null ? new RawType(bytes, "image/svg+xml") : UnDefType.UNDEF;
                case COLOR:
                    return index.getCategory().getColor();
                case ALERT_LEVEL:
                    return new DecimalType(index.getCategory().ordinal());
            }
        }
        return UnDefType.UNDEF;
    }

    private State getSensitive(@Nullable SensitiveGroup sensitiveGroup, AirQualityData data) {
        if (sensitiveGroup != null) {
            int threshHold = Appreciation.UNHEALTHY_FSG.ordinal();
            for (Pollutant pollutant : Pollutant.values()) {
                Index index = Index.find(data.getIaqiValue(pollutant));
                if (index != null && pollutant.sensitiveGroups.contains(sensitiveGroup)
                        && index.getCategory().ordinal() >= threshHold) {
                    return OnOffType.ON;
                }
            }
            return OnOffType.OFF;
        }
        return UnDefType.NULL;
    }

    private State getValue(String channelId, @Nullable String groupId, AirQualityData data) {
        switch (channelId) {
            case TEMPERATURE:
                double temp = data.getIaqiValue("t");
                return temp != -1 ? new QuantityType<>(temp, SIUnits.CELSIUS) : UnDefType.NULL;
            case PRESSURE:
                double press = data.getIaqiValue("p");
                return press != -1 ? new QuantityType<>(press, HECTO(SIUnits.PASCAL)) : UnDefType.NULL;
            case HUMIDITY:
                double hum = data.getIaqiValue("h");
                return hum != -1 ? new QuantityType<>(hum, Units.PERCENT) : UnDefType.NULL;
            case TIMESTAMP:
                return data.getTime()
                        .map(time -> (State) new DateTimeType(
                                time.getObservationTime().withZoneSameLocal(timeZoneProvider.getTimeZone())))
                        .orElse(UnDefType.NULL);
            case DOMINENT:
                return new StringType(data.getDominentPol());
            case DEW_POINT:
                double dp = data.getIaqiValue("dew");
                return dp != -1 ? new QuantityType<>(dp, SIUnits.CELSIUS) : UnDefType.NULL;
            case WIND_SPEED:
                double w = data.getIaqiValue("w");
                return w != -1 ? new QuantityType<>(w, Units.METRE_PER_SECOND) : UnDefType.NULL;
            default:
                if (groupId != null) {
                    double idx = -1;
                    Pollutant pollutant = null;
                    if (AQI.equals(groupId)) {
                        idx = data.getAqi();
                    } else {
                        pollutant = Pollutant.valueOf(groupId.toUpperCase());
                        idx = data.getIaqiValue(pollutant);
                    }
                    return indexedValue(channelId, idx, pollutant);
                }
                return UnDefType.NULL;
        }
    }

    private byte @Nullable [] getResource(String iconPath) {
        try (InputStream stream = classLoader.getResourceAsStream(iconPath)) {
            return stream != null ? stream.readAllBytes() : null;
        } catch (IOException e) {
            logger.warn("Unable to load ressource '{}' : {}", iconPath, e.getMessage());
        }
        return null;
    }
}
