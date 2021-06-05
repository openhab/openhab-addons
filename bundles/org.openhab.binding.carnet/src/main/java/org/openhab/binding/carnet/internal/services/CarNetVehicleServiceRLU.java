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
package org.openhab.binding.carnet.internal.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_REMOTE_LOCK_UNLOCK;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory.CarNetRluLockActionList.CarNetRluLockAction;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.library.types.OnOffType;

/**
 * {@link CarNetVehicleServiceRLU} implements remote vehicle lock/unlock and history.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetVehicleServiceRLU extends CarNetVehicleBaseService {
    public CarNetVehicleServiceRLU(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(thingHandler, api);
        serviceId = CNAPI_SERVICE_REMOTE_LOCK_UNLOCK;
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        if (getConfig().vehicle.numActionHistory == 0) {
            return false;
        }

        addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_LOCK, ITEMT_SWITCH, null, false, false);
        return update(channels);
    }

    private boolean createChannels(Map<String, ChannelIdMapEntry> ch, int index) {
        boolean a = false;
        if (getConfig().vehicle.numActionHistory == 0) {
            return false;
        }
        String group = CHANNEL_GROUP_RLUHIST + index;
        a |= addChannel(ch, group, CHANNEL_RLUHIST_OP, ITEMT_STRING, null, false, true);
        a |= addChannel(ch, group, CHANNEL_RLUHIST_TS, ITEMT_DATETIME, null, false, true);
        a |= addChannel(ch, group, CHANNEL_RLUHIST_RES, ITEMT_STRING, null, false, true);
        return a;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        return update(null);
    }

    private boolean update(@Nullable Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        try {
            CarNetRluHistory hist = api.getRluActionHistory();
            int num = getConfig().vehicle.numActionHistory;
            int i = hist.actions.action.size() - 1; // latest first
            int l = 1;
            while ((i > 0) && (l <= num)) {
                if (channels != null) {
                    createChannels(channels, l);
                } else {
                    CarNetRluLockAction entry = hist.actions.action.get(i);
                    String group = CHANNEL_GROUP_RLUHIST + l;
                    updateChannel(group, CHANNEL_RLUHIST_TS, getDateTime(getString(entry.timestamp)));
                    updateChannel(group, CHANNEL_RLUHIST_OP, getStringType(entry.operation));
                    updateChannel(group, CHANNEL_RLUHIST_RES, getStringType(entry.rluResult));
                }
                i--;
                l++;
                return true;
            }
        } catch (CarNetException e) {

        }
        return false;
    }

    public String lockDoors(OnOffType onOff) throws CarNetException {
        return api.controlLock(onOff == OnOffType.ON);
    }
}
