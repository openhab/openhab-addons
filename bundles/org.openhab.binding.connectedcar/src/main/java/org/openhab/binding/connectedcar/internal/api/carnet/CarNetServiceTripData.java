/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_REMOTE_TRIP_STATISTICS;

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
import org.openhab.binding.connectedcar.internal.handler.VehicleCarNetHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceTripData} implements the drip data service (short-term + long-term).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceTripData extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceTripData.class);

    public CarNetServiceTripData(VehicleCarNetHandler thingHandler, CarNetApi api) {
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
            /*
             * addChannel(ch, group, CHANNEL_TRIP_TIME, ITEMT_DATETIME, null, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_AVG_ELCON, ITEMT_ENERGY, Units.KILOWATT_HOUR, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_AVG_FUELCON, ITEMT_VOLUME, Units.LITRE, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_AVG_SPEED, ITEMT_SPEED, SIUnits.KILOMETRE_PER_HOUR, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_START_MIL, ITEMT_DISTANCE, KILOMETRE, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_MILAGE, ITEMT_DISTANCE, KILOMETRE, false, true);
             * addChannel(ch, group, CHANNEL_TRIP_OVR_MILAGE, ITEMT_DISTANCE, KILOMETRE, true, true);
             */
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
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_FUELCON, new DecimalType(fuel), 1, Units.LITRE);
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_ELCON,
                    new DecimalType(getInteger(entry.averageElectricEngineConsumption) * 100 / 1000), 3,
                    Units.KILOWATT_HOUR); // convert kw per km to kw/h per 100km
            updated |= updateChannel(group, CHANNEL_TRIP_AVG_SPEED, getDecimal(entry.averageSpeed), 1,
                    SIUnits.KILOMETRE_PER_HOUR);
            updated |= updateChannel(group, CHANNEL_TRIP_START_MIL, getDecimal(entry.startMileage), KILOMETRE);
            updated |= updateChannel(group, CHANNEL_TRIP_MILAGE, getDecimal(entry.mileage), KILOMETRE);
            updated |= updateChannel(group, CHANNEL_TRIP_OVR_MILAGE, getDecimal(entry.overallMileage), KILOMETRE);
        }
        return updated;
    }
}
