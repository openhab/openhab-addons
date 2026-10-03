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
package org.openhab.binding.modbus.internal.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.handler.ModbusPoller2ThingHandler;
import org.openhab.binding.modbus.internal.ModbusChannelTypeProvider;
import org.openhab.binding.modbus.internal.handler.Poller2WriteRequest;
import org.openhab.binding.modbus.internal.handler.Poller2WriteResult;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPoller2ActionsTest {

    @Test
    public void submitsHoldingScalarToBoundPoller() {
        ModbusPoller2ThingHandler handler = mock(ModbusPoller2ThingHandler.class);
        ModbusPoller2Actions actions = new ModbusPoller2Actions();
        actions.setThingHandler(handler);
        Poller2WriteRequest request = Poller2WriteRequest.holding("setpoint", new DecimalType("12.5"));
        when(handler.submitActionWrite(request)).thenReturn(Poller2WriteResult.ACCEPTED);

        Poller2WriteResult result = actions.writeHolding("setpoint", 12.5);

        assertEquals(Poller2WriteResult.ACCEPTED, result);
        verify(handler).submitActionWrite(request);
    }

    @Test
    public void submitsCoilValueToBoundPoller() {
        ModbusPoller2ThingHandler handler = mock(ModbusPoller2ThingHandler.class);
        ModbusPoller2Actions actions = new ModbusPoller2Actions();
        actions.setThingHandler(handler);
        Poller2WriteRequest request = Poller2WriteRequest.coil("enable", OnOffType.ON);
        when(handler.submitActionWrite(request)).thenReturn(Poller2WriteResult.ACCEPTED);

        Poller2WriteResult result = actions.writeCoil("enable", true);

        assertEquals(Poller2WriteResult.ACCEPTED, result);
        verify(handler).submitActionWrite(request);
    }

    @Test
    public void rejectsBlankChannelIdWithoutSubmitting() {
        ModbusPoller2Actions actions = new ModbusPoller2Actions();

        assertEquals(Poller2WriteResult.INVALID_COMMAND, actions.writeHolding("", 12));
    }

    @Test
    public void pollerProvidesThingActionsService() {
        ModbusPoller2ThingHandler handler = new ModbusPoller2ThingHandler(mock(Bridge.class),
                mock(ModbusChannelTypeProvider.class));

        assertTrue(handler.getServices().contains(ModbusPoller2Actions.class));
    }
}
