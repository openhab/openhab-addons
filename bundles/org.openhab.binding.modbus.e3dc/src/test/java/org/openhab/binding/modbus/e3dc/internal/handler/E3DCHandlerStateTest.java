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
package org.openhab.binding.modbus.e3dc.internal.handler;

import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

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

        HashMap<String, Object> map = new HashMap<String, Object>();
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
        ByteBuffer infoWrap = ByteBuffer.wrap(infoBlockBytes);
        ModbusRegister[] infoBlock = new ModbusRegister[infoBlockBytes.length / 2];
        for (int i = 0; i < infoBlock.length; i++) {
            infoBlock[i] = new ModbusRegister(infoWrap.get(), infoWrap.get());
        }
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusReadResult(readRequest, new ModbusRegisterArray(infoBlock));
    }

    private AsyncModbusReadResult getDataResult() {
        byte[] dataBlockBytes = new byte[] { 0, -14, 0, 0, -2, -47, -1, -1, 2, 47, 0, 0, 0, 14, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 99, 99, 0, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 125, 2, 21, 0, 0, 0, 27, 0, 26, 0, 0, 0, 103, 0, -117, 0, 0 };
        ByteBuffer dataWrap = ByteBuffer.wrap(dataBlockBytes);
        ModbusRegister[] dataBlock = new ModbusRegister[dataBlockBytes.length / 2];
        for (int i = 0; i < dataBlock.length; i++) {
            dataBlock[i] = new ModbusRegister(dataWrap.get(), dataWrap.get());
        }
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusReadResult(readRequest, new ModbusRegisterArray(dataBlock));
    }

    private AsyncModbusFailure<ModbusReadRequestBlueprint> getFailResult() {
        ModbusReadRequestBlueprint readRequest = mock(ModbusReadRequestBlueprint.class);
        return new AsyncModbusFailure<ModbusReadRequestBlueprint>(readRequest, new Exception("Something failed!"));
    }
}
