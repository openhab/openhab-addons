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
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_SERVICE_REMOTE_LOCK_UNLOCK;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNEluActionHistory.CarNetRluHistory.CarNetRluLockActionList.CarNetRluLockAction;
import org.openhab.binding.connectedcar.internal.handler.VehicleCarNetHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceRLU} implements remote vehicle lock/unlock and history.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceRLU extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceRLU.class);

    public CarNetServiceRLU(VehicleCarNetHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_LOCK_UNLOCK, thingHandler, api);
    }

    @Override
    public boolean isEnabled() {
        return getConfig().vehicle.numRluHistory > 0 && super.isEnabled();
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        addChannels(channels, CHANNEL_GROUP_CONTROL, true, CHANNEL_CONTROL_LOCK);
        int count = getConfig().vehicle.numRluHistory;
        for (int i = 1; i <= count; i++) {
            String group = CHANNEL_GROUP_RLUHIST + i;
            addChannels(channels, group, true, CHANNEL_RLUHIST_OP, CHANNEL_RLUHIST_TS, CHANNEL_RLUHIST_RES);
        }
        return true;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        boolean updated = false;
        CarNetRluHistory hist = ((CarNetApi) api).getRluActionHistory();
        Collections.sort(hist.actions.action, Collections.reverseOrder(new Comparator<CarNetRluLockAction>() {
            @Override
            public int compare(CarNetRluLockAction a, CarNetRluLockAction b) {
                return a.timestamp.compareTo(b.timestamp);
            }
        }));

        int i = 0;
        int count = getConfig().vehicle.numRluHistory;
        for (CarNetRluLockAction entry : hist.actions.action) {
            if (++i > count) {
                break;
            }
            String group = CHANNEL_GROUP_RLUHIST + i;
            updated |= updateChannel(group, CHANNEL_RLUHIST_TS, getDateTime(getString(entry.timestamp)));
            updated |= updateChannel(group, CHANNEL_RLUHIST_OP, getStringType(entry.operation));
            updated |= updateChannel(group, CHANNEL_RLUHIST_RES, getStringType(entry.rluResult));
        }
        return updated;
    }
}
