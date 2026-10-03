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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.ModbusPoller2ThingHandler;
import org.openhab.binding.modbus.internal.handler.Poller2WriteRequest;
import org.openhab.binding.modbus.internal.handler.Poller2WriteResult;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Typed write actions for poller2 things.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = ModbusPoller2Actions.class)
@ThingActionsScope(name = "modbus")
public class ModbusPoller2Actions implements ThingActions, ThingHandlerService {
    private @Nullable ModbusPoller2ThingHandler handler;

    public Poller2WriteResult writeHolding(String channelId, Number value) {
        if (channelId.isBlank()) {
            return Poller2WriteResult.INVALID_COMMAND;
        }
        ModbusPoller2ThingHandler localHandler = handler;
        return localHandler == null ? Poller2WriteResult.HANDLER_UNAVAILABLE
                : localHandler.submitActionWrite(Poller2WriteRequest.holding(channelId,
                        new org.openhab.core.library.types.DecimalType(value.toString())));
    }

    public Poller2WriteResult writeCoil(String channelId, boolean value) {
        if (channelId.isBlank()) {
            return Poller2WriteResult.INVALID_COMMAND;
        }
        ModbusPoller2ThingHandler localHandler = handler;
        return localHandler == null ? Poller2WriteResult.HANDLER_UNAVAILABLE
                : localHandler.submitActionWrite(
                        Poller2WriteRequest.coil(channelId, value ? org.openhab.core.library.types.OnOffType.ON
                                : org.openhab.core.library.types.OnOffType.OFF));
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler thingHandler) {
        handler = thingHandler instanceof ModbusPoller2ThingHandler pollerHandler ? pollerHandler : null;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
