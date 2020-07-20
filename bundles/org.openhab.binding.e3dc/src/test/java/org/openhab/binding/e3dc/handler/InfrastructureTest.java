/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCHandlerFactory;
import org.openhab.binding.e3dc.internal.handler.E3DCDeviceThingHandler;
import org.openhab.binding.e3dc.internal.handler.E3DCInfoHandler;
import org.openhab.binding.e3dc.internal.modbus.ModbusCallback;
import org.openhab.io.transport.modbus.ModbusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link InfrastructureTest} TestInfrastructure of Bridge, Handler and ModbusCallback
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class InfrastructureTest {
    private final Logger logger = LoggerFactory.getLogger(InfrastructureTest.class);

    @Test
    public void testSuppoertedThings() {
        ModbusManager mm = mock(ModbusManager.class);
        E3DCHandlerFactory hf = new E3DCHandlerFactory(mm);
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_DEVICE));
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_INFO));
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_POWER));
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_WALLBOX));
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_STRING_DETAILS));
        assertTrue(hf.supportsThingType(E3DCBindingConstants.THING_TYPE_E3DC_EMERGENCY_POWER));
    }

    @Test
    public void testE3DCInfoDevice() {
        Thing t = mock(Thing.class);
        ThingUID tuid = new ThingUID(E3DCBindingConstants.THING_TYPE_E3DC_DEVICE, "e3dc");
        when(t.getBridgeUID()).thenReturn(tuid);
        when(t.getUID()).thenReturn(tuid);
        E3DCInfoHandler ih = new E3DCInfoHandler(t);
        ThingHandlerCallback callbackHandler = mock(ThingHandlerCallback.class);
        ih.setCallback(callbackHandler);
        ModbusManager mm = mock(ModbusManager.class);
        Bridge b = mock(Bridge.class);
        E3DCDeviceThingHandler dth = new E3DCDeviceThingHandler(b, mm);
        when(callbackHandler.getBridge(tuid)).thenReturn(b);
        when(b.getHandler()).thenReturn(dth);
        assertFalse(ih.isUpdated);
        ih.initialize();
        while (!ih.isInitialized) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("InterruptedException during Handler initialization {}", e.getMessage());
            }
        }
        // now everything should be set up - sent some data and check if isUpdated flag is true
        assertNotNull(dth.getInfoDataProvider());
        ModbusCallback mc = (ModbusCallback) dth.getInfoDataProvider();
        byte[] infoBlock = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69, 68,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48, 52, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        mc.setArray(infoBlock);
        assertTrue(ih.isUpdated);
    }
}
