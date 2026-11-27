/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiError;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class AirCareCapabilityTest {

    private static final ThingTypeUID HOME_COACH = new ThingTypeUID("netatmo", "home-coach");
    private static final String DEVICE_ID = "70:ee:50:00:00:01";

    private @Mock @NonNullByDefault({}) CommonInterface handler;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) AircareApi api;

    @Test
    void serverErrorStartsTheRetries() throws NetatmoException {
        when(thing.getUID()).thenReturn(new ThingUID(HOME_COACH, "coach"));
        when(thing.getThingTypeUID()).thenReturn(HOME_COACH);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getId()).thenReturn(DEVICE_ID);
        when(api.getHomeCoach(DEVICE_ID)).thenThrow(new NetatmoException(new ApiError(), 503, "27"));
        RefreshAutoCapability refresh = new RefreshAutoCapability(handler);
        CapabilityMap capabilities = new CapabilityMap();
        capabilities.put(refresh);
        when(handler.getCapabilities()).thenReturn(capabilities);

        assertTrue(new AirCareCapability(handler).updateReadings(api).isEmpty());

        assertTrue(refresh.isRetrying());
    }
}
