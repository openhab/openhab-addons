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
package org.openhab.binding.carnet.internal.api.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_REMOTE_TRIP_STATISTICS;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetTripData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetTripData.CarNetTripDataList.CarNetTripDataEntry;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
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
public class CarNetServiceTripData extends CarNetBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceTripData.class);

    public CarNetServiceTripData(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_REMOTE_TRIP_STATISTICS, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        try {
            boolean updated = false;
            if (getConfig().vehicle.numShortTrip > 0) {
                updated |= update("shortTerm", channels);
            }
            if (getConfig().vehicle.numLongTrip > 0) {
                updated |= update("longTerm", channels);
            }
            return updated;
        } catch (CarNetException e) {
            logger.debug("{}: Unable to create channels for service {}", thingId, serviceId);
        }
        return false;
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, String type, int index) {
        boolean a = false;
        String group = CHANNEL_GROUP_TRIP_PRE + type + index;
        a |= addChannel(ch, group, CHANNEL_TRIP_TIME, ITEMT_DATETIME, null, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_AVG_ELCON, ITEMT_ENERGY, Units.KILOWATT_HOUR, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_AVG_FUELCON, ITEMT_VOLUME, Units.LITRE, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_AVG_SPEED, ITEMT_SPEED, SIUnits.KILOMETRE_PER_HOUR, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_START_MIL, ITEMT_DISTANCE, KILOMETRE, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_MILAGE, ITEMT_DISTANCE, KILOMETRE, false, true);
        a |= addChannel(ch, group, CHANNEL_TRIP_OVR_MILAGE, ITEMT_DISTANCE, KILOMETRE, true, true);
        return a;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        boolean updated = update("shortTerm", null);
        return updated | update("longTerm", null);
    }

    private boolean update(String type, @Nullable Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        boolean updated = false;
        CarNetTripData std = api.getTripData(type);
        Collections.sort(std.tripDataList.tripData, Collections.reverseOrder(new Comparator<CarNetTripDataEntry>() {
            @Override
            public int compare(CarNetTripDataEntry a, CarNetTripDataEntry b) {
                return a.timestamp.compareTo(b.timestamp);
            }
        }));

        CarNetCombinedConfig config = getConfig();
        boolean shortTerm = type.contains("short");
        int numTrips = shortTerm ? config.vehicle.numShortTrip : config.vehicle.numLongTrip;

        int i = 0; // latest first
        int l = 1;
        while ((i < std.tripDataList.tripData.size()) && (l <= numTrips)) {
            if (channels != null) {
                createChannels(channels, shortTerm ? CHANNEL_TRIP_SHORT : CHANNEL_TRIP_LONG, l);
                updated = true;
            } else {
                String group = (shortTerm ? CHANNEL_GROUP_STRIP : CHANNEL_GROUP_LTRIP) + l;
                CarNetTripDataEntry entry = std.tripDataList.tripData.get(i);
                if (entry != null) {
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
                    updated |= updateChannel(group, CHANNEL_TRIP_OVR_MILAGE, getDecimal(entry.overallMileage),
                            KILOMETRE);
                }
            }
            i++;
            l++;
        }
        return updated;
    }
}
