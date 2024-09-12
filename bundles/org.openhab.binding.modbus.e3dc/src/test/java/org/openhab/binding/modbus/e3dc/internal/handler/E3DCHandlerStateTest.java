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
package org.openhab.binding.modbus.e3dc.internal.handler;

import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link E3DCHandlerStateTest} Test State handling of Handler if different results occurs
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCHandlerStateTest {
    ThingStatusInfo unknownStatus = new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
    ThingStatusInfo onlineStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    ThingStatusInfo offlineStatus = new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            E3DCThingHandler.DATA_READ_ERROR);

    @Test
    public void testStatusChain() {
        Bridge bridge = mock(Bridge.class);
        ThingUID uid = new ThingUID("modbus", "e3dc", "powerplant");
        when(bridge.getUID()).thenReturn(uid);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCThingHandler handler = new E3DCThingHandler(bridge);
        handler.setCallback(callback);

        HashMap<String, Object> map = new HashMap<>();
        map.put("refresh", 2000);
        Configuration config = new Configuration(map);
        when(bridge.getConfiguration()).thenReturn(config);
        handler.initialize();
        verify(callback).statusUpdated(ArgumentMatchers.eq((Thing) bridge), ArgumentMatchers.eq(unknownStatus));
        // Initializing is ongoing - now simulate info and data callback

        handler.handleInfoResult(getInfoResult());
        verify(callback).statusUpdated(ArgumentMatchers.eq((Thing) bridge), ArgumentMatchers.eq(unknownStatus));
        handler.handleDataResult(getDataResult());
        verify(callback, times(1)).statusUpdated(ArgumentMatchers.eq((Thing) bridge),
                ArgumentMatchers.eq(onlineStatus));
        // we are ONLINE!

        // call it a few times - ensure at the end of the test that "ONLINE" status is raised only 2 times and not all
        // the time
        handler.handleDataResult(getDataResult());
        handler.handleDataResult(getDataResult());
        handler.handleDataResult(getDataResult());

        // simulate one wrong data result
        handler.handleDataFailure(getFailResult());
        verify(callback).statusUpdated(ArgumentMatchers.eq((Thing) bridge), ArgumentMatchers.eq(offlineStatus));

        // // go online again
        handler.handleDataResult(getDataResult());
        verify(callback, times(2)).statusUpdated(ArgumentMatchers.eq((Thing) bridge),
                ArgumentMatchers.eq(onlineStatus));
    }

    private AsyncModbusReadResult getInfoResult() {
        byte[] infoBlockBytes = new byte[] { -29, -36, 1, 2, 0, -120, 69, 51, 47, 68, 67, 32, 71, 109, 98, 72, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 32, 69, 32, 65, 73, 79, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 85, 78, 73, 78, 73, 84, 73, 65, 76, 73, 90, 69,
                68, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 49, 48, 95, 50, 48, 50, 48, 95, 48, 52,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusReadResult(readRequest, new ModbusRegisterArray(infoBlockBytes));
    }

    private AsyncModbusReadResult getDataResult() {
        byte[] dataBlockBytes = new byte[] { 0, -14, 0, 0, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusReadResult(readRequest, new ModbusRegisterArray(dataBlockBytes));
    }

    private AsyncModbusFailure<ModbusReadRequestBlueprint> getFailResult() {
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusFailure<>(readRequest, new Exception("Something failed!"));
    }
}
