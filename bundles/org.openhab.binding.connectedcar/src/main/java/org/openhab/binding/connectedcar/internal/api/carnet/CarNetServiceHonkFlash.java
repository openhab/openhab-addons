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
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;

/**
 * {@link CarNetServiceHonkFlash} implements honk&flash service.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceHonkFlash extends ApiBaseService {
    public CarNetServiceHonkFlash(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_REMOTE_HONK_AND_FLASH, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        // Honk&Flash requires CarFinder service to get geo position
        if (api.isRemoteServiceAvailable(CNAPI_SERVICE_CAR_FINDER)) {
            return addChannels(channels, CHANNEL_GROUP_CONTROL, true, CHANNEL_CONTROL_FLASH, CHANNEL_CONTROL_HONKFLASH,
                    CHANNEL_CONTROL_FLASH, CHANNEL_CONTROL_HFDURATION);
        }
        return false;
    }
}
