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
package org.openhab.binding.deutschebahn.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.deutschebahn.internal.timetable.TimeproviderStub;
import org.openhab.binding.deutschebahn.internal.timetable.TimetablesV1ImplTestHelper;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TimetableStop;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for {@link DeutscheBahnTimetableHandler}.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
public class DeutscheBahnTimetableHandlerTest implements TimetablesV1ImplTestHelper {

    private static Configuration createConfig() {
        final Configuration config = new Configuration();
        config.put("accessToken", "letMeIn");
        config.put("evaNo", "8000226");
        config.put("trainFilter", "all");
        return config;
    }

    private static Bridge mockBridge() {
        final Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(DeutscheBahnBindingConstants.TIMETABLE_TYPE, "timetable"));
        when(bridge.getConfiguration()).thenReturn(createConfig());

        final List<Thing> things = new ArrayList<>();
        things.add(DeutscheBahnTrainHandlerTest.mockThing(1));
        things.add(DeutscheBahnTrainHandlerTest.mockThing(2));
        things.add(DeutscheBahnTrainHandlerTest.mockThing(3));
        when(things.get(0).getHandler()).thenReturn(mock(DeutscheBahnTrainHandler.class));
        when(things.get(1).getHandler()).thenReturn(mock(DeutscheBahnTrainHandler.class));
        when(things.get(2).getHandler()).thenReturn(mock(DeutscheBahnTrainHandler.class));

        when(bridge.getThings()).thenReturn(things);

        return bridge;
    }

    private DeutscheBahnTimetableHandler createAndInitHandler(final ThingHandlerCallback callback, final Bridge bridge)
            throws Exception {
        final TimeproviderStub timeProvider = new TimeproviderStub();
        timeProvider.time = new GregorianCalendar(2021, Calendar.AUGUST, 16, 9, 30);

        final DeutscheBahnTimetableHandler handler = new DeutscheBahnTimetableHandler(bridge,
                createApiWithTestdata().getApiFactory(), timeProvider);
        handler.setCallback(callback);
        handler.initialize();
        return handler;
    }

    @Test
    public void testUpdateChannels() throws Exception {
        final Bridge bridge = mockBridge();
        final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        final DeutscheBahnTimetableHandler handler = createAndInitHandler(callback, bridge);

        try {
            verify(callback).statusUpdated(eq(bridge), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
            verify(callback, timeout(1000)).statusUpdated(eq(bridge),
                    argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

            verifyThingUpdated(bridge, 0, "-5296516961807204721-2108160906-5");
            verifyThingUpdated(bridge, 1, "-8364795265993682073-2108160911-6");
            verifyThingUpdated(bridge, 2, "-2949440726131702047-2108160858-10");
        } finally {
            handler.dispose();
        }
    }

    private void verifyThingUpdated(final Bridge bridge, int offset, String stopId) {
        final Thing train = bridge.getThings().get(offset);
        final DeutscheBahnTrainHandler childHandler = (DeutscheBahnTrainHandler) train.getHandler();
        verify(childHandler, timeout(1000))
                .updateChannels(argThat((TimetableStop stop) -> stop.getId().equals(stopId)));
    }
}
