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
package org.openhab.binding.bmwcardata.internal.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataAccountHandler;
import org.openhab.binding.bmwcardata.internal.handler.BMWCarDataVehicleHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Tests which Things the {@link AuthorizationServlet} shows as accounts.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class AuthorizationServletTest {

    @Test
    void showsTheAccountsWithHandlerSortedByLabel() {
        Thing zeta = account("zeta", "Zeta", true);
        Thing alpha = account("alpha", "alpha", false);
        // an account Thing whose handler is not initialized yet, e.g. while the binding starts
        Thing uninitialized = stub(mock(Bridge.class), new ThingUID(THING_TYPE_ACCOUNT, "uninitialized"),
                "Uninitialized", null);
        Thing vehicle = stub(mock(Thing.class), new ThingUID(THING_TYPE_VEHICLE, "car"), "Car",
                mock(BMWCarDataVehicleHandler.class));

        List<AuthorizationPage.Account> accounts = AuthorizationServlet
                .accounts(List.of(zeta, uninitialized, vehicle, alpha));

        assertEquals(List.of("alpha", "Zeta"), accounts.stream().map(AuthorizationPage.Account::label).toList());
        assertFalse(accounts.get(0).authorized());
        assertTrue(accounts.get(1).authorized());
        assertEquals("ONLINE", accounts.get(1).status());
    }

    @Test
    void showsNoAccountWithoutAccountThings() {
        assertTrue(AuthorizationServlet.accounts(List.of()).isEmpty());
    }

    private static Thing account(String id, String label, boolean authorized) {
        BMWCarDataAccountHandler handler = mock(BMWCarDataAccountHandler.class);
        Bridge bridge = stub(mock(Bridge.class), new ThingUID(THING_TYPE_ACCOUNT, id), label, handler);
        when(handler.getThing()).thenReturn(bridge);
        when(handler.isAuthorized()).thenReturn(authorized);
        return bridge;
    }

    private static <T extends Thing> T stub(T thing, ThingUID uid, String label, @Nullable ThingHandler handler) {
        when(thing.getUID()).thenReturn(uid);
        when(thing.getLabel()).thenReturn(label);
        when(thing.getHandler()).thenReturn(handler);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        return thing;
    }
}
