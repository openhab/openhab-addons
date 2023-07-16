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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_SERVICE_REMOTE_TRIP_STATISTICS;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetTripData;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetTripData.CarNetTripDataList.CarNetTripDataEntry;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DecimalType;

/**
 * {@link CarNetServiceTripData} implements the drip data service (short-term + long-term).
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceTripData extends ApiBaseService {
    public CarNetServiceTripData(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_TRIP_STATISTICS, thingHandler, api);
    }

    @Override
    public boolean isEnabled() {
        CombinedConfig config = getConfig();
        return (config.vehicle.numShortTrip > 0 || config.vehicle.numLongTrip > 0) && super.isEnabled();
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        CombinedConfig config = getConfig();
        boolean created = false;
        created |= createChannels(channels, CHANNEL_TRIP_SHORT, config.vehicle.numShortTrip);
        created |= createChannels(channels, CHANNEL_TRIP_LONG, config.vehicle.numLongTrip);
        return created;
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, String type, int count) {
        if (count == 0) {
            return false;
        }

        boolean created = false;
        for (int i = 1; i <= count; i++) {
            String group = CHANNEL_GROUP_TRIP_PRE + type + i;
            created |= addChannels(ch, group, true, CHANNEL_TRIP_TIME, CHANNEL_TRIP_AVG_ELCON, CHANNEL_TRIP_AVG_FUELCON,
                    CHANNEL_TRIP_AVG_SPEED, CHANNEL_TRIP_START_MIL, CHANNEL_TRIP_MILAGE, CHANNEL_TRIP_OVR_MILAGE);
        }
        return created;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = update("shortTerm", null);
        return updated | update("longTerm", null);
    }

    private boolean update(String type, @Nullable Map<String, ChannelIdMapEntry> channels) throws ApiException {
        boolean updated = false;
        CarNetTripData std = ((CarNetApi) api).getTripData(type);
        Collections.sort(std.tripDataList.tripData, Collections.reverseOrder(new Comparator<CarNetTripDataEntry>() {
            @Override
            public int compare(CarNetTripDataEntry a, CarNetTripDataEntry b) {
                return a.timestamp.compareTo(b.timestamp);
            }
        }));

        CombinedConfig config = getConfig();
        boolean shortTerm = type.contains("short");
        int numTrips = shortTerm ? config.vehicle.numShortTrip : config.vehicle.numLongTrip, i = 0;
        for (CarNetTripDataEntry entry : std.tripDataList.tripData) {
            if (++i > numTrips) {
                break;
            }

            String group = (shortTerm ? CHANNEL_GROUP_STRIP : CHANNEL_GROUP_LTRIP) + i;
            double fuel = getDouble(entry.averageFuelConsumption) / 10.0; // convert dL to l
            updated |= updateChannel(group, CHANNEL_TRIP_TIME, getDateTime(getString(entry.timestamp)));
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_FUELCON, new DecimalType(fuel));
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_ELCON,
                    new DecimalType(getInteger(entry.averageElectricEngineConsumption) * 100 / 1000)); // convert kw per
                                                                                                       // km to kw/h per
                                                                                                       // 100km
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_SPEED, getDecimal(entry.averageSpeed));
            updated |= updateChannel(group, CHANNEL_TRIP_START_MIL, getDecimal(entry.startMileage));
            updated |= updateChannel(group, CHANNEL_TRIP_MILAGE, getDecimal(entry.mileage));
            updated |= updateChannel(group, CHANNEL_TRIP_OVR_MILAGE, getDecimal(entry.overallMileage));
        }
        return updated;
    }
}
