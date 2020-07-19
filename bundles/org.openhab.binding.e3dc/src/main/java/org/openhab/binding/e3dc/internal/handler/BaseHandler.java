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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} Basic Handler for all things
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHandler extends BaseThingHandler implements DataListener {
    private final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private @Nullable ThingHandlerCallback thingHandlerCallback;
    private @Nullable ModbusDataProvider modbusDataProvider;

    public BaseHandler(Thing thing) {
        super(thing);
    }

    public void initialize(DataType t) {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            Bridge b = getBridge();
            if (b != null) {
                BridgeHandler bridgeHandler = b.getHandler();
                if (bridgeHandler != null) {
                    if (t.equals(DataType.INFO)) {
                        ModbusDataProvider localModbusDataProvider = ((E3DCDeviceThingHandler) bridgeHandler)
                                .getInfoDataProvider();
                        modbusDataProvider = localModbusDataProvider;
                        localModbusDataProvider.addDataListener(this);
                        updateStatus(ThingStatus.ONLINE);
                    } else if (t.equals(DataType.DATA)) {
                        ModbusDataProvider localModbusDataProvider = ((E3DCDeviceThingHandler) bridgeHandler)
                                .getDataProvider();
                        modbusDataProvider = localModbusDataProvider;
                        localModbusDataProvider.addDataListener(this);
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    logger.warn("BridgeHandler not found");
                }
            } else {
                logger.warn("Bridge not found");
            }
        });
    }

    @Override
    public void dispose() {
        ModbusDataProvider localModbusDataProvider = modbusDataProvider;
        if (localModbusDataProvider != null) {
            localModbusDataProvider.removeDataListener(this);
        }
    }

    @Override
    public void setCallback(@Nullable ThingHandlerCallback thingHandlerCallback) {
        super.setCallback(thingHandlerCallback);
        this.thingHandlerCallback = thingHandlerCallback;
        logger.info("ThingCallbackHandler received {}", thingHandlerCallback);
    }

    @Override
    public @Nullable Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            ThingHandlerCallback localThinghandlerCallback = thingHandlerCallback;
            if (localThinghandlerCallback != null) {
                return bridgeUID != null ? localThinghandlerCallback.getBridge(bridgeUID) : null;
            } else {
                logger.warn("Handler {} of thing {} isn't able to resolve bridger", getClass().getSimpleName(),
                        thing.getUID());
                return null;
            }
        }
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public abstract void dataAvailable(ModbusDataProvider provider);
}
