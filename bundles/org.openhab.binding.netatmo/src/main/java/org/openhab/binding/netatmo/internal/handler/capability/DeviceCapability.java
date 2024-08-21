/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;

/**
 * The {@link DeviceCapability} takes care of handling properties for netatmo devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceCapability extends Capability {
    private static final int DATA_AGE_LIMIT_S = 3600;

    /**
     * Whether the device is owned or not by the user (a favorite station or a guest station is not owned by the user).
     * It will be updated when handling the result of the getstationsdata API.
     * It must be initialized to false to be sure that the first call to the API will not fail for a favorite/guest
     * weather stations.
     */
    protected boolean owned;

    public DeviceCapability(CommonInterface handler) {
        super(handler);
    }

    @Override
    protected void updateNAMain(NAMain newData) {
        owned = !newData.isReadOnly();
        if (firstLaunch) {
            newData.getPlace().ifPresent(place -> {
                place.getCity().map(city -> properties.put(PROPERTY_CITY, city));
                place.getCountry().map(country -> properties.put(PROPERTY_COUNTRY, country));
                place.getTimezone().map(tz -> properties.put(PROPERTY_TIMEZONE, tz));
            });
        }
        if (!newData.hasFreshData(DATA_AGE_LIMIT_S)) {
            statusReason = "@text/data-over-limit";
        }
    }
}
