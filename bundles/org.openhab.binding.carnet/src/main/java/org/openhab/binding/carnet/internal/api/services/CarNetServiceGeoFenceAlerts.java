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
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_GEOFENCING;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts.CarNetGeoFenceAlertEntry;
import org.openhab.binding.carnet.internal.api.CarNetChannelIdMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceGeoFenceAlerts} implements geofence service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceGeoFenceAlerts extends CarNetBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceGeoFenceAlerts.class);

    public CarNetServiceGeoFenceAlerts(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_GEOFENCING, thingHandler, api);
    }

    @Override
    public boolean isEnabled() {
        return (getConfig().vehicle.numGeoFenceAlerts > 0) && super.isEnabled();
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        int count = getConfig().vehicle.numGeoFenceAlerts;
        boolean created = false;
        for (int index = 1; index <= count; index++) {
            String group = CHANNEL_GROUP_GEOFENCE + index;
            created |= addChannel(channels, group, CHANNEL_GEOFENCE_TYPE, ITEMT_STRING, null, false, true);
            created |= addChannel(channels, group, CHANNEL_GEOFENCE_TIME, ITEMT_DATETIME, null, false, true);
            created |= addChannel(channels, group, CHANNEL_GEOFENCE_DESCR, ITEMT_STRING, null, false, true);
        }
        return created;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        CarNetGeoFenceAlerts gfa = api.getGeoFenceAlerts();
        if (gfa.geofencingAlert == null) {
            return false;
        }
        Collections.sort(gfa.geofencingAlert, Collections.reverseOrder(new Comparator<CarNetGeoFenceAlertEntry>() {
            @Override
            public int compare(CarNetGeoFenceAlertEntry a, CarNetGeoFenceAlertEntry b) {
                return a.occurenceDateTime.compareTo(b.occurenceDateTime);
            }
        }));

        boolean updated = false;
        int i = 0; // latest first
        int count = getConfig().vehicle.numGeoFenceAlerts;
        for (CarNetGeoFenceAlertEntry entry : gfa.geofencingAlert) {
            if (++i > count) {
                break;
            }

            String group = CHANNEL_GROUP_GEOFENCE + i;
            updated |= updateChannel(group, CHANNEL_GEOFENCE_TIME, getDateTime(getString(entry.occurenceDateTime)));
            updated |= updateChannel(group, CHANNEL_GEOFENCE_TYPE, getStringType(entry.alertType));
            updated |= updateChannel(group, CHANNEL_GEOFENCE_DESCR, getStringType(entry.definitionName));
        }
        return updated;
    }
}
