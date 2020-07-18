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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} Basic Handler for all things
 *
 * @author Bernd Weymann - Initial contribution
 */
public abstract class BaseHandler extends BaseThingHandler implements DataListener {
    private final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private ThingHandlerCallback thingHandlerCallback;
    private ModbusDataProvider modbusDataProvider;

    public BaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            Bridge b = getBridge();
            // logger.info("Bridge? {}", b);
            BridgeHandler bridgeHandler = b.getHandler();
            // logger.info("Got BridgeHandler {}, Is Provider? {}", bridgeHandler,
            // bridgeHandler instanceof E3DCDeviceThingHandler);
            modbusDataProvider = ((E3DCDeviceThingHandler) bridgeHandler).getDataProvider();
            if (modbusDataProvider != null) {
                modbusDataProvider.addDataListener(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        if (modbusDataProvider != null) {
            modbusDataProvider.removeDataListener(this);
        }
    }

    @Override
    public void setCallback(@Nullable ThingHandlerCallback thingHandlerCallback) {
        // TODO Auto-generated method stub
        super.setCallback(thingHandlerCallback);
        this.thingHandlerCallback = thingHandlerCallback;
        logger.info("ThingCallbackHandler received {}", thingHandlerCallback);
    }

    @Override
    public Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            if (thingHandlerCallback != null) {
                return bridgeUID != null ? thingHandlerCallback.getBridge(bridgeUID) : null;
            } else {
                logger.warn(
                        "Handler {} of thing {} tried accessing its bridge although the handler was already disposed.",
                        getClass().getSimpleName(), thing.getUID());
                return null;
            }
        }
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public abstract void dataAvailable(@NonNull ModbusDataProvider provider);
}
