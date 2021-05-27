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
import static org.openhab.binding.carnet.internal.CarNetUtils.getStringType;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_DESTINATIONS;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations.CarNetDestination;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNDestinations.CarNetDestinationList;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.UnDefType;

/**
 * {@link CarNetServiceDestinations} implements the destination hostory
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceDestinations extends CarNetBaseService {
    public CarNetServiceDestinations(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_DESTINATIONS, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        if (getConfig().vehicle.numDestinations > 0) {
            try {
                update(channels);
                return true;
            } catch (CarNetException e) {
            }
        }
        return false;
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, int index) {
        boolean a = false;
        String group = CHANNEL_GROUP_DEST_PRE + index;
        a |= addChannel(ch, group, CHANNEL_DEST_NAME, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_POI, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_GEO, ITEMT_LOCATION, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_STREET, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_CITY, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_ZIP, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_DEST_COUNTY, ITEMT_STRING, null, true, true);
        a |= addChannel(ch, group, CHANNEL_DEST_SOURCE, ITEMT_STRING, null, true, true);
        return a;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        boolean updated = false;
        CarNetDestinationList dest = api.getDestinations();
        if (dest.destination.size() == 0) {
            // no/empty list
            return false;
        }

        Collections.sort(dest.destination, Collections.reverseOrder(new Comparator<CarNetDestination>() {
            @Override
            public int compare(CarNetDestination a, CarNetDestination b) {
                return a.id.compareTo(b.id);
            }
        }));

        int numDest = getConfig().vehicle.numDestinations;
        int i = 0; // latest first
        int l = 1;
        while ((i < dest.destination.size()) && (l <= numDest)) {
            if (channels != null) {
                createChannels(channels, l);
            } else {
                String group = CHANNEL_GROUP_DEST_PRE + l;
                CarNetDestination entry = dest.destination.get(i);
                if (entry != null) {
                    updated |= updateChannel(group, CHANNEL_DEST_NAME, getStringType(entry.destinationName));
                    updated |= updateChannel(group, CHANNEL_DEST_POI,
                            entry.POIContact != null ? getStringType(entry.POIContact.lastName) : UnDefType.UNDEF);
                    updated |= updateChannel(group, CHANNEL_DEST_SOURCE, getStringType(entry.destinationSource));

                    if (entry.address != null) {
                        updated |= updateChannel(group, CHANNEL_DEST_STREET, getStringType(entry.address.street));
                        updated |= updateChannel(group, CHANNEL_DEST_CITY, getStringType(entry.address.city));
                        updated |= updateChannel(group, CHANNEL_DEST_ZIP, getStringType(entry.address.zipCode));
                        updated |= updateChannel(group, CHANNEL_DEST_COUNTY, getStringType(entry.address.country));
                    } else {
                        updated |= updateChannel(group, CHANNEL_DEST_STREET, UnDefType.UNDEF);
                        updated |= updateChannel(group, CHANNEL_DEST_CITY, UnDefType.UNDEF);
                        updated |= updateChannel(group, CHANNEL_DEST_ZIP, UnDefType.UNDEF);
                        updated |= updateChannel(group, CHANNEL_DEST_COUNTY, UnDefType.UNDEF);
                    }

                    if ((entry.geoCoordinate != null) && (entry.geoCoordinate.getLattitude() != 0)) {
                        PointType location = new PointType(new DecimalType(entry.geoCoordinate.getLattitude()),
                                new DecimalType(entry.geoCoordinate.getLongitude()));
                        updated |= updateChannel(group, CHANNEL_DEST_GEO, location);
                    } else {
                        updated |= updateChannel(group, CHANNEL_DEST_GEO, UnDefType.UNDEF);
                    }
                }
            }
            i++;
            l++;
        }
        return updated;
    }
}
