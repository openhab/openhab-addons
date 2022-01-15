/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vigicrues.internal.handler;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vigicrues.internal.StationConfiguration;
import org.openhab.binding.vigicrues.internal.api.ApiHandler;
import org.openhab.binding.vigicrues.internal.api.VigiCruesException;
import org.openhab.binding.vigicrues.internal.dto.hubeau.HubEauResponse;
import org.openhab.binding.vigicrues.internal.dto.opendatasoft.OpenDatasoftResponse;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.CdStationHydro;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.InfoVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TerEntVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TronEntVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.VicANMoinsUn;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VigiCruesHandler} is responsible for querying the API and
 * updating channels
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VigiCruesHandler.class);
    private final LocationProvider locationProvider;

    private @Nullable ScheduledFuture<?> refreshJob;
    private final ApiHandler apiHandler;

    private List<QuantityType<?>> referenceHeights = new ArrayList<>();
    private List<QuantityType<?>> referenceFlows = new ArrayList<>();
    private @Nullable String portion;

    public VigiCruesHandler(Thing thing, LocationProvider locationProvider, ApiHandler apiHandler) {
        super(thing);
        this.apiHandler = apiHandler;
        this.locationProvider = locationProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VigiCrues handler.");

        StationConfiguration config = getConfigAs(StationConfiguration.class);
        logger.debug("config refresh = {} min", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);

        if (thing.getProperties().isEmpty()) {
            Map<String, String> properties = discoverAttributes(config);
            updateProperties(properties);
        }
        getReferences();
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES);
    }

    private void getReferences() {
        List<QuantityType<?>> heights = new ArrayList<>();
        List<QuantityType<?>> flows = new ArrayList<>();
        thing.getProperties().keySet().stream().filter(key -> key.startsWith(FLOOD)).forEach(key -> {
            String value = thing.getProperties().get(key);
            if (value != null) {
                if (key.contains(FLOW)) {
                    flows.add(new QuantityType<>(value));
                } else {
                    heights.add(new QuantityType<>(value));
                }
            }
        });
        referenceHeights = heights.stream().distinct().sorted().collect(Collectors.toList());
        referenceFlows = flows.stream().distinct().sorted().collect(Collectors.toList());
        portion = thing.getProperties().get(TRONCON);
    }

    private Map<String, String> discoverAttributes(StationConfiguration config) {
        Map<String, String> properties = new HashMap<>();

        ThingBuilder thingBuilder = editThing();
        List<Channel> channels = new ArrayList<>(getThing().getChannels());

        try {
            HubEauResponse stationDetails = apiHandler.discoverStations(config.id);
            stationDetails.stations.stream().findFirst().ifPresent(station -> {
                PointType stationLocation = new PointType(
                        String.format(Locale.US, "%f,%f", station.latitudeStation, station.longitudeStation));
                properties.put(LOCATION, stationLocation.toString());
                PointType serverLocation = locationProvider.getLocation();
                if (serverLocation != null) {
                    DecimalType distance = serverLocation.distanceFrom(stationLocation);
                    properties.put(DISTANCE, new QuantityType<>(distance, SIUnits.METRE).toString());
                }
                properties.put(RIVER, station.libelleCoursEau);
            });
        } catch (VigiCruesException e) {
            logger.info("Unable to retrieve station location details {} : {}", config.id, e.getMessage());
        }

        try {
            CdStationHydro refineStation = apiHandler.getStationDetails(config.id);
            if (refineStation.vigilanceCrues.cruesHistoriques == null) {
                throw new VigiCruesException("No historical data available");
            }
            refineStation.vigilanceCrues.cruesHistoriques.stream()
                    .forEach(crue -> properties.putAll(crue.getDescription()));
            String codeTerritoire = refineStation.vigilanceCrues.pereBoitEntVigiCru.cdEntVigiCru;
            TerEntVigiCru territoire = apiHandler.getTerritoire(codeTerritoire);
            for (VicANMoinsUn troncon : territoire.vicTerEntVigiCru.vicANMoinsUn) {
                TronEntVigiCru detail = apiHandler.getTroncon(troncon.vicCdEntVigiCru);
                if (detail.getStations().anyMatch(s -> config.id.equalsIgnoreCase(s.vicCdEntVigiCru))) {
                    properties.put(TRONCON, troncon.vicCdEntVigiCru);
                    break;
                }
            }
        } catch (VigiCruesException e) {
            logger.info("Historical flooding data are not available {} : {}", config.id, e.getMessage());
            channels.removeIf(channel -> channel.getUID().getId().contains(RELATIVE_PREFIX));
        }

        if (!properties.containsKey(TRONCON)) {
            channels.removeIf(channel -> channel.getUID().getId().contains(ALERT));
            channels.removeIf(channel -> channel.getUID().getId().contains(COMMENT));
        }

        try {
            OpenDatasoftResponse measures = apiHandler.getMeasures(config.id);
            measures.getFirstRecord().ifPresent(field -> {
                if (field.getHeight() == -1) {
                    channels.removeIf(channel -> (channel.getUID().getId().contains(HEIGHT)));
                }
                if (field.getFlow() == -1) {
                    channels.removeIf(channel -> (channel.getUID().getId().contains(FLOW)));
                }
            });
        } catch (VigiCruesException e) {
            logger.warn("Unable to read measurements data {} : {}", config.id, e.getMessage());
        }

        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());

        return properties;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the VigiCrues handler.");

        ScheduledFuture<?> localJob = refreshJob;
        if (localJob != null) {
            localJob.cancel(true);
        }
        refreshJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        }
    }

    private void updateAndPublish() {
        StationConfiguration config = getConfigAs(StationConfiguration.class);
        try {
            OpenDatasoftResponse measures = apiHandler.getMeasures(config.id);
            measures.getFirstRecord().ifPresent(field -> {
                double height = field.getHeight();
                if (height != -1) {
                    updateQuantity(HEIGHT, height, SIUnits.METRE);
                    updateRelativeMeasure(RELATIVE_HEIGHT, referenceHeights, height);
                }

                double flow = field.getFlow();
                if (flow != -1) {
                    updateQuantity(FLOW, flow, Units.CUBICMETRE_PER_SECOND);
                    updateRelativeMeasure(RELATIVE_FLOW, referenceFlows, flow);
                }

                field.getTimestamp().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
            });
            String currentPortion = portion;
            if (currentPortion != null) {
                InfoVigiCru status = apiHandler.getTronconStatus(currentPortion);
                updateAlert(ALERT, status.vicInfoVigiCru.vicNivInfoVigiCru - 1);
                updateString(SHORT_COMMENT, status.vicInfoVigiCru.vicSituActuInfoVigiCru);
                updateString(COMMENT, status.vicInfoVigiCru.vicQualifInfoVigiCru);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (VigiCruesException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateString(String channelId, String value) {
        if (isLinked(channelId)) {
            updateState(channelId, new StringType(value));
        }
    }

    private void updateRelativeMeasure(String channelId, List<QuantityType<?>> reference, double value) {
        if (!reference.isEmpty()) {
            double percent = value / reference.get(0).doubleValue() * 100;
            updateQuantity(channelId, percent, Units.PERCENT);
        }
    }

    private void updateQuantity(String channelId, Double value, Unit<?> unit) {
        if (isLinked(channelId)) {
            updateState(channelId, new QuantityType<>(value, unit));
        }
    }

    private void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId, new DateTimeType(zonedDateTime));
        }
    }

    private void updateAlert(String channelId, int value) {
        String channelIcon = channelId + "-icon";
        if (isLinked(channelId)) {
            updateState(channelId, new DecimalType(value));
        }
        if (isLinked(channelIcon)) {
            byte[] resource = getResource(String.format("picto/crue-%d.svg", value));
            updateState(channelIcon, resource != null ? new RawType(resource, "image/svg+xml") : UnDefType.UNDEF);
        }
    }

    private byte @Nullable [] getResource(String iconPath) {
        ClassLoader classLoader = VigiCruesHandler.class.getClassLoader();
        if (classLoader != null) {
            try (InputStream stream = classLoader.getResourceAsStream(iconPath)) {
                return stream != null ? stream.readAllBytes() : null;
            } catch (IOException e) {
                logger.warn("Unable to load ressource '{}' : {}", iconPath, e.getMessage());
            }
        }
        return null;
    }
}
