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

    /**
     * Writes one holding-register channel value.
     *
     * @param channelId configured holding-register channel id
     * @param value scalar value to write
     * @return whether the write was accepted for asynchronous execution
     */
    public Poller2WriteResult writeHolding(String channelId, Number value) {
        return writeHolding(channelId, value, false);
    }

    /**
     * Writes one holding-register channel value, optionally using FC16 for this single write.
     *
     * @param channelId configured holding-register channel id
     * @param value scalar value to write
     * @param writeMultiple whether to use the Modbus write-multiple function code for this single write
     * @return whether the write was accepted for asynchronous execution
     */
    public Poller2WriteResult writeHolding(String channelId, Number value, boolean writeMultiple) {
        if (channelId.isBlank()) {
            return Poller2WriteResult.INVALID_COMMAND;
        }
        ModbusPoller2ThingHandler localHandler = handler;
        return localHandler == null ? Poller2WriteResult.HANDLER_UNAVAILABLE
                : localHandler.submitActionWrite(Poller2WriteRequest.holding(channelId,
                        new org.openhab.core.library.types.DecimalType(value.toString()), writeMultiple));
    }

    /**
     * Writes one coil channel value.
     *
     * @param channelId configured coil channel id
     * @param value scalar value to write
     * @return whether the write was accepted for asynchronous execution
     */
    public Poller2WriteResult writeCoil(String channelId, boolean value) {
        return writeCoil(channelId, value, false);
    }

    /**
     * Writes one coil channel value, optionally using FC15 for this single write.
     *
     * @param channelId configured coil channel id
     * @param value scalar value to write
     * @param writeMultiple whether to use the Modbus write-multiple function code for this single write
     * @return whether the write was accepted for asynchronous execution
     */
    public Poller2WriteResult writeCoil(String channelId, boolean value, boolean writeMultiple) {
        if (channelId.isBlank()) {
            return Poller2WriteResult.INVALID_COMMAND;
        }
        ModbusPoller2ThingHandler localHandler = handler;
        return localHandler == null ? Poller2WriteResult.HANDLER_UNAVAILABLE
                : localHandler.submitActionWrite(
                        Poller2WriteRequest.coil(channelId, value ? org.openhab.core.library.types.OnOffType.ON
                                : org.openhab.core.library.types.OnOffType.OFF, writeMultiple));
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
