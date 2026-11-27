/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitVehiclesConfiguration;
import org.openhab.binding.transit.internal.net.dto.VehiclesResult;
import org.openhab.binding.transit.internal.net.dto.VehiclesResult.Vehicle;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link TransitVehiclesHandler} shows the real-time positions of the vehicles of a route.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitVehiclesHandler extends TransitPollingHandler<VehiclesResult> {

    private static final List<String> VEHICLE_CHANNELS = List.of(CHANNEL_LOCATION, CHANNEL_LAST_UPDATE,
            CHANNEL_DIRECTION_ID, CHANNEL_OCCUPANCY_STATUS, CHANNEL_WHEELCHAIR_ACCESSIBLE, CHANNEL_VEHICLE_LABEL);

    private TransitVehiclesConfiguration config = new TransitVehiclesConfiguration();

    public TransitVehiclesHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitVehiclesConfiguration.class);
        if (config.routeId.isBlank()) {
            return "@text/offline.conf-error-no-route-id";
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected VehiclesResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        Integer directionId = config.directionId == 0 || config.directionId == 1 ? config.directionId : null;
        return bridgeHandler.getVehicles(config.routeId.trim(), directionId);
    }

    @Override
    protected void publish(VehiclesResult result, long nowSeconds) {
        // sort by vehicle ID so that a vehicle keeps its channel group between updates as long as possible
        List<Vehicle> vehicles = new ArrayList<>(result.getVehicles());
        vehicles.sort(Comparator.comparing(vehicle -> Objects.requireNonNullElse(vehicle.getVehicleId(), "")));

        updateGroupState(CHANNEL_GROUP_VEHICLES, CHANNEL_VEHICLE_COUNT, new DecimalType(vehicles.size()));
        Instant newestUpdate = vehicles.stream().map(Vehicle::getUpdatedAt).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        updateGroupState(CHANNEL_GROUP_VEHICLES, CHANNEL_LAST_UPDATE, toDateTimeState(newestUpdate));

        int groupIdx = 1;
        for (Vehicle vehicle : vehicles) {
            if (groupIdx > MAX_CHANNEL_GROUPS) {
                break;
            }
            String group = CHANNEL_GROUP_VEHICLE_PREFIX + groupIdx;
            updateGroupState(group, CHANNEL_LOCATION, toLocationState(vehicle));
            updateGroupState(group, CHANNEL_LAST_UPDATE, toDateTimeState(vehicle.getUpdatedAt()));
            updateGroupState(group, CHANNEL_DIRECTION_ID, toDecimalState(vehicle.getDirectionId()));
            updateGroupState(group, CHANNEL_OCCUPANCY_STATUS, toDecimalState(vehicle.getOccupancyStatus()));
            updateGroupState(group, CHANNEL_WHEELCHAIR_ACCESSIBLE,
                    toWheelchairState(vehicle.getWheelchairAccessible()));
            updateGroupState(group, CHANNEL_VEHICLE_LABEL, toStringState(vehicle.getVehicleLabel()));
            groupIdx++;
        }
        clearRemainingVehicles(groupIdx);
    }

    @Override
    protected void onCommunicationError() {
        updateGroupState(CHANNEL_GROUP_VEHICLES, CHANNEL_VEHICLE_COUNT, UnDefType.UNDEF);
        updateGroupState(CHANNEL_GROUP_VEHICLES, CHANNEL_LAST_UPDATE, UnDefType.UNDEF);
        clearRemainingVehicles(1);
    }

    private void clearRemainingVehicles(int startIdx) {
        for (int i = startIdx; i <= MAX_CHANNEL_GROUPS; i++) {
            String group = CHANNEL_GROUP_VEHICLE_PREFIX + i;
            VEHICLE_CHANNELS.forEach(channel -> updateGroupState(group, channel, UnDefType.UNDEF));
        }
    }

    private static State toLocationState(Vehicle vehicle) {
        Double latitude = vehicle.getLatitude();
        Double longitude = vehicle.getLongitude();
        if (latitude == null || longitude == null) {
            return UnDefType.UNDEF;
        }
        return new PointType(new DecimalType(latitude), new DecimalType(longitude));
    }

    private static State toDecimalState(@Nullable Integer value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value);
    }
}
