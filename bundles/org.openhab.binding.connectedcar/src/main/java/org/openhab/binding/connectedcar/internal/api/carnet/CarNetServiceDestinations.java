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
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_SERVICE_DESTINATIONS;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getStringType;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNDestinations.CarNetDestination;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNDestinations.CarNetDestinationList;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.UnDefType;

/**
 * {@link CarNetServiceDestinations} implements the destination hostory
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceDestinations extends ApiBaseService {
    public CarNetServiceDestinations(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_DESTINATIONS, thingHandler, api);
    }

    @Override
    public boolean isEnabled() {
        return getConfig().vehicle.numDestinations > 0 && super.isEnabled();
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        if (getConfig().vehicle.numDestinations > 0) {
            try {
                update(channels);
                return true;
            } catch (ApiException e) {
            }
        }
        return false;
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, int index) {
        String group = CHANNEL_GROUP_DEST_PRE + index;
        return addChannels(ch, group, true, CHANNEL_DEST_NAME, CHANNEL_DEST_POI, CHANNEL_DEST_GEO, CHANNEL_DEST_STREET,
                CHANNEL_DEST_CITY, CHANNEL_DEST_ZIP, CHANNEL_DEST_COUNTY, CHANNEL_DEST_SOURCE);
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws ApiException {
        boolean updated = false;
        CarNetDestinationList dest = ((CarNetApi) api).getDestinations();
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
                CarNetDestination entry = dest.destination.get(i);
                if (entry != null) {
                    updated |= updateChannel(CHANNEL_DEST_NAME, getStringType(entry.destinationName));
                    updated |= updateChannel(CHANNEL_DEST_POI,
                            entry.POIContact != null ? getStringType(entry.POIContact.lastName) : UnDefType.UNDEF);
                    updated |= updateChannel(CHANNEL_DEST_SOURCE, getStringType(entry.destinationSource));

                    if (entry.address != null) {
                        updated |= updateChannel(CHANNEL_DEST_STREET, getStringType(entry.address.street));
                        updated |= updateChannel(CHANNEL_DEST_CITY, getStringType(entry.address.city));
                        updated |= updateChannel(CHANNEL_DEST_ZIP, getStringType(entry.address.zipCode));
                        updated |= updateChannel(CHANNEL_DEST_COUNTY, getStringType(entry.address.country));
                    } else {
                        updated |= updateChannel(CHANNEL_DEST_STREET, UnDefType.UNDEF);
                        updated |= updateChannel(CHANNEL_DEST_CITY, UnDefType.UNDEF);
                        updated |= updateChannel(CHANNEL_DEST_ZIP, UnDefType.UNDEF);
                        updated |= updateChannel(CHANNEL_DEST_COUNTY, UnDefType.UNDEF);
                    }

                    if ((entry.geoCoordinate != null) && (entry.geoCoordinate.getLattitude() != 0)) {
                        PointType location = new PointType(new DecimalType(entry.geoCoordinate.getLattitude()),
                                new DecimalType(entry.geoCoordinate.getLongitude()));
                        updated |= updateChannel(CHANNEL_DEST_GEO, location);
                    } else {
                        updated |= updateChannel(CHANNEL_DEST_GEO, UnDefType.UNDEF);
                    }
                }
            }
            i++;
            l++;
        }
        return updated;
    }
}
