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
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_SPEED_ALERT;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts.CarNetpeedAlertEntry;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceSpeedAlerts} implements speedalert service.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceSpeedAlerts extends CarNetBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceSpeedAlerts.class);

    public CarNetServiceSpeedAlerts(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_SPEED_ALERT, thingHandler, api);
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
        String group = CHANNEL_GROUP_SPEEDALERT + index;
        a |= addChannel(ch, group, CHANNEL_SPEEDALERT_TYPE, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_SPEEDALERT_TIME, ITEMT_DATETIME, null, false, true);
        a |= addChannel(ch, group, CHANNEL_SPEEDALERT_DESCR, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_SPEEDALERT_LIMIT, ITEMT_NUMBER, null, false, true);
        return a;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        boolean updated = false;
        CarNetSpeedAlerts sa = api.getSpeedAlerts();
        if (sa.speedAlert == null) {
            return false;
        }
        Collections.sort(sa.speedAlert, Collections.reverseOrder(new Comparator<CarNetpeedAlertEntry>() {
            @Override
            public int compare(CarNetpeedAlertEntry a, CarNetpeedAlertEntry b) {
                return a.occurenceDateTime.compareTo(b.occurenceDateTime);
            }
        }));

        int i = 0; // latest first
        int l = 1;
        while ((i < sa.speedAlert.size()) && (l <= getConfig().vehicle.numSpeedAlerts)) {
            if (channels != null) {
                createChannels(channels, l);
                updated = true;
            } else {
                String group = CHANNEL_GROUP_SPEEDALERT + l;
                CarNetpeedAlertEntry entry = sa.speedAlert.get(i);
                if (entry != null) {
                    updated |= updateChannel(group, CHANNEL_SPEEDALERT_TYPE, getStringType(entry.alertType));
                    updated |= updateChannel(group, CHANNEL_SPEEDALERT_DESCR, getStringType(entry.definitionName));
                    updated |= updateChannel(group, CHANNEL_SPEEDALERT_TIME,
                            getDateTime(getString(entry.occurenceDateTime)));
                    updated |= updateChannel(group, CHANNEL_SPEEDALERT_LIMIT, getDecimal(entry.speedLimit));
                }
            }
            i++;
            l++;
        }
        return updated;
    }
}
