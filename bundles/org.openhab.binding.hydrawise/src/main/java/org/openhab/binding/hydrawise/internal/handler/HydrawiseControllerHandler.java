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
package org.openhab.binding.hydrawise.internal.handler;

import static org.openhab.binding.hydrawise.internal.HydrawiseBindingConstants.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hydrawise.internal.HydrawiseControllerListener;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.graphql.HydrawiseGraphQLClient;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Controller;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Forecast;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Sensor;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.UnitValue;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.Zone;
import org.openhab.binding.hydrawise.internal.api.graphql.dto.ZoneRun;
import org.openhab.binding.hydrawise.internal.config.HydrawiseControllerConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HydrawiseControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */

@NonNullByDefault
public class HydrawiseControllerHandler extends BaseThingHandler implements HydrawiseControllerListener {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseControllerHandler.class);
    private static final int DEFAULT_SUSPEND_TIME_HOURS = 24;
    private static final int DEFAULT_REFRESH_SECONDS = 15;
    // All responses use US local time formats
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM uu HH:mm:ss Z",
            Locale.US);
    private final Map<String, @Nullable State> stateMap = Collections
            .synchronizedMap(new HashMap<String, @Nullable State>());
    private final Map<String, @Nullable Zone> zoneMaps = Collections
            .synchronizedMap(new HashMap<String, @Nullable Zone>());
    private int controllerId;

    public HydrawiseControllerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        HydrawiseControllerConfiguration config = getConfigAs(HydrawiseControllerConfiguration.class);
        controllerId = config.controllerId;
        HydrawiseAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.addControllerListeners(this);
            Bridge bridge = getBridge();
            if (bridge != null) {
                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Controller Handler disposed.");
        HydrawiseAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.removeControllerListeners(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand channel {} Command {}", channelUID.getAsString(), command.toFullString());
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Controller is NOT ONLINE and is not responding to commands");
            return;
        }

        // remove our cached state for this, will be safely updated on next poll
        stateMap.remove(channelUID.getAsString());

        if (command instanceof RefreshType) {
            // we already removed this from the cache
            return;
        }

        HydrawiseGraphQLClient client = apiClient();
        if (client == null) {
            logger.debug("API client not found");
            return;
        }

        String group = channelUID.getGroupId();
        String channelId = channelUID.getIdWithoutGroup();
        boolean allCommand = CHANNEL_GROUP_ALLZONES.equals(group);
        Zone zone = zoneMaps.get(group);

        if (!allCommand && zone == null) {
            logger.debug("Zone not found {}", group);
            return;
        }

        try {
            switch (channelId) {
                case CHANNEL_ZONE_RUN_CUSTOM:
                    if (!(command instanceof QuantityType<?>)) {
                        logger.warn("Invalid command type for run custom {}", command.getClass().getName());
                        return;
                    }
                    QuantityType<?> time = ((QuantityType<?>) command).toUnit(Units.SECOND);

                    if (time == null) {
                        return;
                    }

                    if (allCommand) {
                        client.runAllRelays(controllerId, time.intValue());
                    } else if (zone != null) {
                        client.runRelay(zone.id, time.intValue());
                    }
                    break;
                case CHANNEL_ZONE_RUN:
                    if (!(command instanceof OnOffType)) {
                        logger.warn("Invalid command type for run {}", command.getClass().getName());
                        return;
                    }
                    if (allCommand) {
                        if (command == OnOffType.ON) {
                            client.runAllRelays(controllerId);
                        } else {
                            client.stopAllRelays(controllerId);
                        }
                    } else if (zone != null) {
                        if (command == OnOffType.ON) {
                            client.runRelay(zone.id);
                        } else {
                            client.stopRelay(zone.id);
                        }
                    }
                    break;
                case CHANNEL_ZONE_SUSPEND:
                    if (!(command instanceof OnOffType)) {
                        logger.warn("Invalid command type for suspend {}", command.getClass().getName());
                        return;
                    }
                    if (allCommand) {
                        if (command == OnOffType.ON) {
                            client.suspendAllRelays(controllerId, OffsetDateTime.now(ZoneOffset.UTC)
                                    .plus(DEFAULT_SUSPEND_TIME_HOURS, ChronoUnit.HOURS).format(DATE_FORMATTER));
                        } else {
                            client.resumeAllRelays(controllerId);
                        }
                    } else if (zone != null) {
                        if (command == OnOffType.ON) {
                            client.suspendRelay(zone.id, OffsetDateTime.now(ZoneOffset.UTC)
                                    .plus(DEFAULT_SUSPEND_TIME_HOURS, ChronoUnit.HOURS).format(DATE_FORMATTER));
                        } else {
                            client.resumeRelay(zone.id);
                        }
                    }
                    break;
                case CHANNEL_ZONE_SUSPENDUNTIL:
                    if (!(command instanceof DateTimeType)) {
                        logger.warn("Invalid command type for suspend {}", command.getClass().getName());
                        return;
                    }
                    if (allCommand) {
                        client.suspendAllRelays(controllerId,
                                ((DateTimeType) command).getZonedDateTime().format(DATE_FORMATTER));
                    } else if (zone != null) {
                        client.suspendRelay(zone.id,
                                ((DateTimeType) command).getZonedDateTime().format(DATE_FORMATTER));
                    }
                    break;
                default:
                    logger.warn("Uknown channelId {}", channelId);
                    return;
            }
            HydrawiseAccountHandler handler = getAccountHandler();
            if (handler != null) {
                handler.refreshData(DEFAULT_REFRESH_SECONDS);
            }
        } catch (HydrawiseCommandException | HydrawiseConnectionException e) {
            logger.debug("Could not issue command", e);
        } catch (HydrawiseAuthenticationException e) {
            logger.debug("Credentials not valid");
        }
    }

    @Override
    public void onData(List<Controller> controllers) {
        logger.trace("onData my controller id {}", controllerId);
        controllers.stream().filter(c -> c.id == controllerId).findFirst().ifPresent(controller -> {
            logger.trace("Updating Controller {} sensors {} forecast {} ", controller.id, controller.sensors,
                    controller.location.forecast);
            updateController(controller);
            if (controller.sensors != null) {
                updateSensors(controller.sensors);
            }
            if (controller.location != null && controller.location.forecast != null) {
                updateForecast(controller.location.forecast);
            }
            if (controller.zones != null) {
                updateZones(controller.zones, controller.hardware.model.maxZones);
            }

            // update values with what the cloud tells us even though the controller may be offline
            if (!controller.status.online) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Service reports controller as offline");
            } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached value so the new channel gets updated on the next poll
        stateMap.remove(channelUID.getId());
    }

    private void updateController(Controller controller) {
        updateGroupState(CHANNEL_GROUP_CONTROLLER_SYSTEM, CHANNEL_CONTROLLER_NAME, new StringType(controller.name));
        updateGroupState(CHANNEL_GROUP_CONTROLLER_SYSTEM, CHANNEL_CONTROLLER_SUMMARY,
                new StringType(controller.status.summary));
        updateGroupState(CHANNEL_GROUP_CONTROLLER_SYSTEM, CHANNEL_CONTROLLER_LAST_CONTACT,
                controller.status.lastContact != null ? secondsToDateTime(controller.status.lastContact.timestamp)
                        : UnDefType.NULL);
    }

    private void updateZones(List<Zone> zones, int maxZones) {
        AtomicReference<Boolean> anyRunning = new AtomicReference<>(false);
        AtomicReference<Boolean> anySuspended = new AtomicReference<>(false);
        for (Zone zone : zones) {
            // for expansion modules who zones numbers are > 99
            // there are maxZones relays per expander, expanders will have a zoneNumber like:
            // maxZones = 12
            // 10 for expander 0, relay 10 = zone10
            // 101 for expander 1, relay 1 = zone13
            // 212 for expander 2, relay 12 = zone36
            // division of integers in Java give whole numbers, not remainders FYI
            int zoneNumber = zone.number.value <= 99 ? zone.number.value
                    : ((zone.number.value / 100) * maxZones) + (zone.number.value % 100);
            String group = "zone" + zoneNumber;
            zoneMaps.put(group, zone);
            logger.trace("Updating Zone {} {} ", group, zone.name);
            updateGroupState(group, CHANNEL_ZONE_NAME, new StringType(zone.name));
            updateGroupState(group, CHANNEL_ZONE_ICON, new StringType(BASE_IMAGE_URL + zone.icon.fileName));
            if (zone.scheduledRuns != null) {
                updateGroupState(group, CHANNEL_ZONE_SUMMARY,
                        zone.scheduledRuns.summary != null ? new StringType(zone.scheduledRuns.summary)
                                : UnDefType.UNDEF);
                ZoneRun nextRun = zone.scheduledRuns.nextRun;
                if (nextRun != null) {
                    updateGroupState(group, CHANNEL_ZONE_DURATION, new QuantityType<>(nextRun.duration, Units.MINUTE));
                    updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME,
                            secondsToDateTime(nextRun.startTime.timestamp));
                } else {
                    updateGroupState(group, CHANNEL_ZONE_DURATION, UnDefType.UNDEF);
                    updateGroupState(group, CHANNEL_ZONE_NEXT_RUN_TIME_TIME, UnDefType.UNDEF);
                }
                ZoneRun currRunn = zone.scheduledRuns.currentRun;
                if (currRunn != null) {
                    updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.ON);
                    updateGroupState(group, CHANNEL_ZONE_TIME_LEFT, new QuantityType<>(
                            currRunn.endTime.timestamp - Instant.now().getEpochSecond(), Units.SECOND));
                    anyRunning.set(true);
                } else {
                    updateGroupState(group, CHANNEL_ZONE_RUN, OnOffType.OFF);
                    updateGroupState(group, CHANNEL_ZONE_TIME_LEFT, new QuantityType<>(0, Units.MINUTE));
                }
            }
            if (zone.status.suspendedUntil != null) {
                updateGroupState(group, CHANNEL_ZONE_SUSPEND, OnOffType.ON);
                updateGroupState(group, CHANNEL_ZONE_SUSPENDUNTIL,
                        secondsToDateTime(zone.status.suspendedUntil.timestamp));
                anySuspended.set(true);
            } else {
                updateGroupState(group, CHANNEL_ZONE_SUSPEND, OnOffType.OFF);
                updateGroupState(group, CHANNEL_ZONE_SUSPENDUNTIL, UnDefType.UNDEF);
            }
        }
        updateGroupState(CHANNEL_GROUP_ALLZONES, CHANNEL_ZONE_RUN, anyRunning.get() ? OnOffType.ON : OnOffType.OFF);
        updateGroupState(CHANNEL_GROUP_ALLZONES, CHANNEL_ZONE_SUSPEND,
                anySuspended.get() ? OnOffType.ON : OnOffType.OFF);
        updateGroupState(CHANNEL_GROUP_ALLZONES, CHANNEL_ZONE_SUSPENDUNTIL, UnDefType.UNDEF);
    }

    private void updateSensors(List<Sensor> sensors) {
        int i = 1;
        for (Sensor sensor : sensors) {
            String group = "sensor" + (i++);
            updateGroupState(group, CHANNEL_SENSOR_NAME, new StringType(sensor.name));
            if (sensor.model.offTimer != null) {
                updateGroupState(group, CHANNEL_SENSOR_OFFTIMER,
                        new QuantityType<>(sensor.model.offTimer, Units.SECOND));
            }
            if (sensor.model.delay != null) {
                updateGroupState(group, CHANNEL_SENSOR_DELAY, new QuantityType<>(sensor.model.delay, Units.SECOND));
            }
            if (sensor.model.offLevel != null) {
                updateGroupState(group, CHANNEL_SENSOR_OFFLEVEL, new DecimalType(sensor.model.offLevel));
            }
            if (sensor.status.active != null) {
                updateGroupState(group, CHANNEL_SENSOR_ACTIVE, OnOffType.from(sensor.status.active));
            }
            if (sensor.status.waterFlow != null) {
                updateGroupState(group, CHANNEL_SENSOR_WATERFLOW,
                        waterFlowToQuantityType(sensor.status.waterFlow.value, sensor.status.waterFlow.unit));
            }
        }
    }

    private void updateForecast(List<Forecast> forecasts) {
        int i = 1;
        for (Forecast forecast : forecasts) {
            String group = "forecast" + (i++);
            logger.trace("Updating {} {}", group, forecast.time);
            updateGroupState(group, CHANNEL_FORECAST_TIME, stringToDateTime(forecast.time));
            updateGroupState(group, CHANNEL_FORECAST_CONDITIONS, new StringType(forecast.conditions));
            updateGroupState(group, CHANNEL_FORECAST_HUMIDITY, new DecimalType(forecast.averageHumidity.intValue()));
            updateTemperature(forecast.highTemperature, group, CHANNEL_FORECAST_TEMPERATURE_HIGH);
            updateTemperature(forecast.lowTemperature, group, CHANNEL_FORECAST_TEMPERATURE_LOW);
            updateWindspeed(forecast.averageWindSpeed, group, CHANNEL_FORECAST_WIND);
            // this seems to sometimes be optional
            if (forecast.evapotranspiration != null) {
                updateGroupState(group, CHANNEL_FORECAST_EVAPOTRANSPRIATION,
                        new DecimalType(forecast.evapotranspiration.value.floatValue()));
            }
            updateGroupState(group, CHANNEL_FORECAST_PRECIPITATION,
                    new DecimalType(forecast.precipitation.value.floatValue()));
            updateGroupState(group, CHANNEL_FORECAST_PROBABILITYOFPRECIPITATION,
                    new DecimalType(forecast.probabilityOfPrecipitation));

        }
    }

    private void updateTemperature(UnitValue temperature, String group, String channel) {
        logger.debug("TEMP {} {} {} {}", group, channel, temperature.unit, temperature.value);
        updateGroupState(group, channel, new QuantityType<Temperature>(temperature.value,
                temperature.unit.indexOf("F") >= 0 ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
    }

    private void updateWindspeed(UnitValue wind, String group, String channel) {
        updateGroupState(group, channel, new QuantityType<Speed>(wind.value,
                "mph".equals(wind.unit) ? ImperialUnits.MILES_PER_HOUR : SIUnits.KILOMETRE_PER_HOUR));
    }

    private void updateGroupState(String group, String channelID, State state) {
        String channelName = group + "#" + channelID;
        State oldState = stateMap.put(channelName, state);
        if (!state.equals(oldState)) {
            ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelName);
            logger.debug("updateState updating {} {}", channelUID, state);
            updateState(channelUID, state);
        }
    }

    @Nullable
    private HydrawiseAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("No bridge found for thing");
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.warn("No handler found for bridge");
            return null;
        }
        return ((HydrawiseAccountHandler) handler);
    }

    @Nullable
    private HydrawiseGraphQLClient apiClient() {
        HydrawiseAccountHandler handler = getAccountHandler();
        if (handler == null) {
            return null;
        } else {
            return handler.graphQLClient();
        }
    }

    private DateTimeType secondsToDateTime(Integer seconds) {
        Instant instant = Instant.ofEpochSecond(seconds);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        return new DateTimeType(zdt);
    }

    private DateTimeType stringToDateTime(String date) {
        ZonedDateTime zdt = ZonedDateTime.parse(date, DATE_FORMATTER);
        return new DateTimeType(zdt);
    }

    private QuantityType<Volume> waterFlowToQuantityType(Number flow, String units) {
        return new QuantityType<>(flow.doubleValue(),
                "gal".equals(units) ? ImperialUnits.GALLON_LIQUID_US : Units.LITRE);
    }
}
