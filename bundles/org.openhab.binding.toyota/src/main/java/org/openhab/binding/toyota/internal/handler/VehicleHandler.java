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

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.toyota.internal.ToyotaException;
import org.openhab.binding.toyota.internal.config.VehicleConfiguration;
import org.openhab.binding.toyota.internal.dto.Doors;
import org.openhab.binding.toyota.internal.dto.Hood;
import org.openhab.binding.toyota.internal.dto.Key;
import org.openhab.binding.toyota.internal.dto.Lamps;
import org.openhab.binding.toyota.internal.dto.Lock;
import org.openhab.binding.toyota.internal.dto.StatusResponse;
import org.openhab.binding.toyota.internal.dto.Vehicle;
import org.openhab.binding.toyota.internal.dto.Windows;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
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

    private @NonNullByDefault({}) VehicleConfiguration configuration;
    private @NonNullByDefault({}) MyTBridgeHandler bridgeHandler;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();

    public VehicleHandler(Thing thing) {
        super(thing);
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
            updateDoorStatus(status.protectionState.doors, status.protectionState.hood);
            updateLampStatus(status.protectionState.lamps);
            updateWindowStatus(status.protectionState.windows);
            updateLockStatus(status.protectionState.doors, status.protectionState.lock);
            updateKeyStatus(status.protectionState.key);
            // Status newVehicleStatus = service.getURL(vehicle.statusURL, Status.class);
            // vehiclePosition = new VehiclePositionWrapper(service.getURL(Position.class, configuration.vin));
            // // Update all channels from the updated data
            // if (newVehicleStatus.odometer != vehicleStatus.odometer) {
            // triggerChannel(GROUP_OTHER + "#" + CAR_EVENT, EVENT_CAR_MOVED);
            // // We will update trips only if car position has changed to save server queries
            // updateTrips(service);
            // }
            // if (!vehicleStatus.getEngineRunning().equals(newVehicleStatus.getEngineRunning())
            // && newVehicleStatus.getEngineRunning().get() == OnOffType.ON) {
            // triggerChannel(GROUP_OTHER + "#" + CAR_EVENT, EVENT_CAR_STARTED);
            // }
            // vehicleStatus = newVehicleStatus;
        } catch (ToyotaException e) {
            logger.warn("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            freeRefreshJob();
            startAutomaticRefresh(configuration.refresh, mytBridgeHandler);
        }
    }

    private void updateKeyStatus(Key key) {
        updateChannelOnOff(GROUP_KEY, IN_CAR, key.inCar);
        updateChannelOnOff(GROUP_KEY, WARNING, key.warning);
    }

    private void updateLockStatus(Doors doors, Lock lock) {
        updateChannelOnOff(GROUP_LOCKS, DRIVER, doors.driverSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCKS, PASSENGER, doors.passengerSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCKS, REAR_RIGHT, doors.rearRightSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCKS, REAR_LEFT, doors.rearLeftSeatDoor.locked);
        updateChannelOnOff(GROUP_LOCKS, TAILGATE, doors.backDoor.locked);
        updateChannelString(GROUP_LOCKS, STATUS, lock.lockState);
        updateChannelString(GROUP_LOCKS, SOURCE, lock.source);
    }

    private void updateWindowStatus(Windows windows) {
        updateChannelOpenClosed(GROUP_WINDOWS, DRIVER, windows.driverSeatWindow.isClosed());
        updateChannelOpenClosed(GROUP_WINDOWS, PASSENGER, windows.passengerSeatWindow.isClosed());
        updateChannelOpenClosed(GROUP_WINDOWS, REAR_RIGHT, windows.rearRightSeatWindow.isClosed());
        updateChannelOpenClosed(GROUP_WINDOWS, REAR_LEFT, windows.rearLeftSeatWindow.isClosed());
    }

    private void updateLampStatus(Lamps lamps) {
        updateChannelOnOff(GROUP_LAMPS, HEAD, !lamps.headLamp.off);
        updateChannelOnOff(GROUP_LAMPS, TAIL, !lamps.tailLamp.off);
        updateChannelOnOff(GROUP_LAMPS, HAZARD, !lamps.hazardLamp.off);
    }

    private void updateDoorStatus(Doors doors, Hood hood) {
        updateChannelOpenClosed(GROUP_DOORS, DRIVER, doors.driverSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOORS, PASSENGER, doors.passengerSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOORS, REAR_RIGHT, doors.rearRightSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOORS, REAR_LEFT, doors.rearLeftSeatDoor.closed);
        updateChannelOpenClosed(GROUP_DOORS, TAILGATE, doors.backDoor.closed);
        updateChannelOpenClosed(GROUP_DOORS, HOOD, hood.closed);
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

    protected void updateChannelOpenClosed(String group, String channelId, boolean closed) {
        updateIfActive(group, channelId, closed ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }

    protected void updateChannelOnOff(String group, String channelId, boolean locked) {
        updateIfActive(group, channelId, OnOffType.from(locked));
    }

    protected void updateChannelString(String group, String channelId, @Nullable String value) {
        updateIfActive(group, channelId, value == null || value.isEmpty() ? UnDefType.NULL : new StringType(value));
    }
}
