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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNGeoFenceAlerts.CarNetGeoFenceAlerts.CarNetGeoFenceAlertEntry;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
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
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        try {
            return update(channels);
        } catch (CarNetException e) {
            logger.debug("{}: Unable to create channels for service {}", thingId, serviceId);
        }
        return false;
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, int index) {
        boolean a = false;
        String group = CHANNEL_GROUP_GEOFENCE + index;
        a |= addChannel(ch, group, CHANNEL_GEOFENCE_TYPE, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_GEOFENCE_TIME, ITEMT_DATETIME, null, false, true);
        a |= addChannel(ch, group, CHANNEL_GEOFENCE_DESCR, ITEMT_STRING, null, false, true);
        return a;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        boolean updated = false;
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

        CarNetCombinedConfig config = getConfig();
        int entries = config.vehicle.numGeoFenceAlerts;

        int i = 0; // latest first
        int l = 1;
        while ((i < gfa.geofencingAlert.size()) && (l <= entries)) {
            if (channels != null) {
                createChannels(channels, l);
                updated = true;
            } else {
                String group = CHANNEL_GROUP_GEOFENCE + l;
                CarNetGeoFenceAlertEntry entry = gfa.geofencingAlert.get(i);
                if (entry != null) {
                    updated |= updateChannel(group, CHANNEL_GEOFENCE_TIME,
                            getDateTime(getString(entry.occurenceDateTime)));
                    updated |= updateChannel(group, CHANNEL_GEOFENCE_TYPE, getStringType(entry.alertType));
                    updated |= updateChannel(group, CHANNEL_GEOFENCE_DESCR, getStringType(entry.definitionName));
                }
            }
            i++;
            l++;
        }
        return updated;
    }
}
