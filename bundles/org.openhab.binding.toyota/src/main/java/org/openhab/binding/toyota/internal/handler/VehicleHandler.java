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
package org.openhab.binding.toyota.internal.handler;

import static org.openhab.binding.toyota.internal.ToyotaBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.toyota.internal.ToyotaException;
import org.openhab.binding.toyota.internal.config.VehicleConfiguration;
import org.openhab.binding.toyota.internal.dto.Doors;
import org.openhab.binding.toyota.internal.dto.Event;
import org.openhab.binding.toyota.internal.dto.Hood;
import org.openhab.binding.toyota.internal.dto.Key;
import org.openhab.binding.toyota.internal.dto.Lamps;
import org.openhab.binding.toyota.internal.dto.Lock;
import org.openhab.binding.toyota.internal.dto.Metrics;
import org.openhab.binding.toyota.internal.dto.StatusResponse;
import org.openhab.binding.toyota.internal.dto.Vehicle;
import org.openhab.binding.toyota.internal.dto.Window.WindowClosingState;
import org.openhab.binding.toyota.internal.dto.Windows;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final TimeZoneProvider timeZoneProvider;

    private @NonNullByDefault({}) VehicleConfiguration configuration;
    private @NonNullByDefault({}) MyTBridgeHandler bridgeHandler;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public VehicleHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Toyota Vehicle handler for {}", getThing().getUID());

        Bridge bridge = getBridge();
        scheduler.submit(() -> initilizeWithBridge(bridge == null ? null : bridge.getHandler(),
                bridge == null ? ThingStatus.OFFLINE : bridge.getStatus()));
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());

        Bridge bridge = getBridge();
        initilizeWithBridge(bridge == null ? null : bridge.getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initilizeWithBridge(@Nullable ThingHandler bridgeHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (bridgeHandler instanceof MyTBridgeHandler mytBridgeHandler) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                configuration = getConfigAs(VehicleConfiguration.class);
                Vehicle me = mytBridgeHandler.getVehicle(configuration.vin);
                if (me != null) {
                    updateStatus(ThingStatus.ONLINE);
                    getThing().setProperty(Vehicle.PRODUCTION_DATE, me.productionDate);
                    startAutomaticRefresh(configuration.refresh, mytBridgeHandler);
                    return;
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/err-no-vehicle-found");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Start the job refreshing the vehicle data
     *
     * @param refresh : refresh frequency in minutes
     * @param service
     */
    private void startAutomaticRefresh(int refresh, MyTBridgeHandler mytBridgeHandler) {
        if (refreshJob.isEmpty() || refreshJob.get().isCancelled()) {
            this.refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(
                    () -> queryApiAndUpdateChannels(mytBridgeHandler), 5, refresh * 60, TimeUnit.SECONDS));
        }
    }

    private void queryApiAndUpdateChannels(MyTBridgeHandler mytBridgeHandler) {
        try {
            StatusResponse status = mytBridgeHandler.getVehicleStatus(configuration.vin);

            updateChannelString(GROUP_GENERAL, OVERALL, status.protectionState.overallStatus.name());
            updateChannelDateTime(GROUP_GENERAL, TIMESTAMP, status.protectionState.timestamp);
            updateChannelString(GROUP_GENERAL, STATUS, status.tripStatus.name());

            updateChannelString(GROUP_CLIMATE, STATUS, status.climate.status);
            updateChannelString(GROUP_CLIMATE, TYPE, status.climate.type);

            updateDoorStatus(status.protectionState.doors, status.protectionState.hood);
            updateLampStatus(status.protectionState.lamps);
            updateWindowStatus(status.protectionState.windows);
            updateLockStatus(status.protectionState.doors, status.protectionState.lock);
            updateKeyStatus(status.protectionState.key);
            updatePositionStatus(status.event);

            List<Metrics> metrics = mytBridgeHandler.getMetrics(configuration.vin);
            metrics.forEach(metric -> {
                switch (metric.type) {
                    case FUEL:
                        updateChannelQuantity(GROUP_METRICS, FUEL, Double.valueOf(metric.value).intValue(),
                                Units.PERCENT);
                        break;
                    case MILEAGE:
                        updateChannelQuantity(GROUP_METRICS, ODOMETER, metric.value, KILO(SIUnits.METRE));
                        break;
                    case UNKNOWN:
                        logger.warn("Unknown metric received : {},{}", metric.value, metric.unit);
                        break;
                }
            });

        } catch (ToyotaException e) {
            logger.warn("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            freeRefreshJob();
            startAutomaticRefresh(configuration.refresh, mytBridgeHandler);
        }
    }

    private void updatePositionStatus(Event event) {
        updateChannelLocation(GROUP_POSITION, LOCATION, event.lat, event.lon);
        updateChannelDateTime(GROUP_POSITION, TIMESTAMP,
                ZonedDateTime.ofInstant(event.timestamp, timeZoneProvider.getTimeZone()));
    }

    private void updateKeyStatus(Key key) {
        updateChannelOnOff(GROUP_KEY, IN_CAR, key.inCar);
        updateChannelOnOff(GROUP_KEY, WARNING, key.warning);
    }

    private void updateLockStatus(Doors doors, Lock lock) {
        updateChannelOnOff(GROUP_LOCK, DRIVER, doors.driverSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCK, PASSENGER, doors.passengerSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCK, REAR_RIGHT, doors.rearRightSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCK, REAR_LEFT, doors.rearLeftSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCK, TAILGATE, doors.backDoor.locked);
        updateChannelString(GROUP_LOCK, STATUS, lock.lockState.name());
        updateChannelString(GROUP_LOCK, SOURCE, lock.source.name());
    }

    private void updateWindowStatus(Windows windows) {
        updateChannelOpenClosed(GROUP_WINDOW, DRIVER, windows.driver.state);
        updateChannelOpenClosed(GROUP_WINDOW, PASSENGER, windows.passenger.state);
        updateChannelOpenClosed(GROUP_WINDOW, REAR_RIGHT, windows.rearRight.state);
        updateChannelOpenClosed(GROUP_WINDOW, REAR_LEFT, windows.rearLeft.state);
    }

    private void updateLampStatus(Lamps lamps) {
        updateChannelOnOff(GROUP_LAMP, HEAD, !lamps.headLamp.off);
        updateChannelOnOff(GROUP_LAMP, TAIL, !lamps.tailLamp.off);
        updateChannelOnOff(GROUP_LAMP, HAZARD, !lamps.hazardLamp.off);
    }

    private void updateDoorStatus(Doors doors, Hood hood) {
        updateChannelOpenClosed(GROUP_DOOR, DRIVER, doors.driverSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOOR, PASSENGER, doors.passengerSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOOR, REAR_RIGHT, doors.rearRightSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOOR, REAR_LEFT, doors.rearLeftSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOOR, TAILGATE, doors.backDoor.closed);
        updateChannelOpenClosed(GROUP_DOOR, HOOD, hood.closed);
    }

    private void freeRefreshJob() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
    }

    @Override
    public void dispose() {
        freeRefreshJob();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
        }
    }

    private void updateIfActive(String group, String channelId, State state) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, state);
        }
    }

    private void updateChannelOpenClosed(String group, String channelId, WindowClosingState closingState) {
        updateIfActive(group, channelId, closingState.state);
    }

    protected void updateChannelOpenClosed(String group, String channelId, boolean closed) {
        updateIfActive(group, channelId, closed ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }

    protected void updateChannelOnOff(String group, String channelId, boolean locked) {
        updateIfActive(group, channelId, OnOffType.from(locked));
    }

    protected void updateChannelString(String group, String channelId, @Nullable String value) {
        updateIfActive(group, channelId, value == null || value.isEmpty() ? UnDefType.NULL : new StringType(value));
    }

    protected void updateChannelDateTime(String group, String channelId, @Nullable ZonedDateTime timestamp) {
        updateIfActive(group, channelId, timestamp == null ? UnDefType.NULL : new DateTimeType(timestamp));
    }

    protected void updateChannelLocation(String group, String channelId, double lat, double lon) {
        updateIfActive(group, channelId, new PointType(String.format(Locale.US, "%.6f,%.6f", lat, lon)));
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        updateIfActive(group, channelId, quantity != null ? quantity : UnDefType.NULL);
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable Number d, Unit<?> unit) {
        if (d == null) {
            updateIfActive(group, channelId, UnDefType.NULL);
        } else {
            updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
        }
    }
}
