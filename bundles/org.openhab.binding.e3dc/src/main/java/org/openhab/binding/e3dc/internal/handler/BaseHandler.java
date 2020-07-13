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
package org.openhab.binding.e3dc.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.E3DCConfiguration;
import org.openhab.io.transport.modbus.ModbusManager;

/**
 * The {@link BaseHandler} Base Handler with funnctions relevant for all Handler
 *
 * @author Bernd Weymann - Initial contribution
 */
public abstract class BaseHandler extends BaseThingHandler {

    protected ModbusManager modbusManagerRef;
    protected ThingStatus myStatus = ThingStatus.UNKNOWN;

    public BaseHandler(Thing thing, ModbusManager ref) {
        super(thing);
        modbusManagerRef = ref;
    }

    protected boolean checkConfig(@Nullable E3DCConfiguration config) {
        if (config != null) {
            if (config.ipAddress != null && config.port > 1) {
                if (config.refreshInterval_sec < 1) {
                    config.refreshInterval_sec = 2;
                }
                return true;
            }
        }
        return false;
    }

    protected void setStatus(ThingStatus status) {
        myStatus = status;
        updateStatus(myStatus);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);
}
