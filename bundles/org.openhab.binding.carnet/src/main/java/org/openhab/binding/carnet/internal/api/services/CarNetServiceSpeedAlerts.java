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
import static org.openhab.binding.carnet.internal.CarUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_SPEED_ALERT;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNSpeedAlerts.CarNetSpeedAlerts.CarNetpeedAlertEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.core.library.unit.SIUnits;
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
    public boolean isEnabled() {
        return (getConfig().vehicle.numSpeedAlerts > 0) && super.isEnabled();
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarException {
        boolean created = false;
        int count = getConfig().vehicle.numSpeedAlerts;
        for (int i = 0; i < count; i++) {
            String group = CHANNEL_GROUP_SPEEDALERT + i;
            created |= addChannel(channels, group, CHANNEL_SPEEDALERT_TYPE, ITEMT_STRING, null, false, true);
            created |= addChannel(channels, group, CHANNEL_SPEEDALERT_TIME, ITEMT_DATETIME, null, false, true);
            created |= addChannel(channels, group, CHANNEL_SPEEDALERT_DESCR, ITEMT_STRING, null, false, true);
            created |= addChannel(channels, group, CHANNEL_SPEEDALERT_LIMIT, ITEMT_SPEED, null, false, true);
        }
        return created;
    }

    @Override
    public boolean serviceUpdate() throws CarException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws CarException {
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

        boolean updated = false;
        int i = 0; // latest first
        int count = getConfig().vehicle.numSpeedAlerts;
        for (CarNetpeedAlertEntry entry : sa.speedAlert) {
            if (++i > count) {
                break;
            }
            String group = CHANNEL_GROUP_SPEEDALERT + i;
            updated |= updateChannel(group, CHANNEL_SPEEDALERT_TYPE, getStringType(entry.alertType));
            updated |= updateChannel(group, CHANNEL_SPEEDALERT_DESCR, getStringType(entry.definitionName));
            updated |= updateChannel(group, CHANNEL_SPEEDALERT_TIME, getDateTime(getString(entry.occurenceDateTime)));
            updated |= updateChannel(group, CHANNEL_SPEEDALERT_LIMIT, getDecimal(entry.speedLimit), 0,
                    SIUnits.KILOMETRE_PER_HOUR);
        }
        return updated;
    }
}
