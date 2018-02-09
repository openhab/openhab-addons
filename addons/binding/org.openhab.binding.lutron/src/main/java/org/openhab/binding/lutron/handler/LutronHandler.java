/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.lutron.internal.protocol.LutronCommand;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.LutronOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base type for all Lutron thing handlers.
 *
 * @author Allan Tong - Initial contribution
 *
 */
public abstract class LutronHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(LutronHandler.class);

    public LutronHandler(Thing thing) {
        super(thing);
    }

    public abstract int getIntegrationId();

    public abstract void handleUpdate(LutronCommandType type, String... parameters);

    protected IPBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        return bridge == null ? null : (IPBridgeHandler) bridge.getHandler();
    }

    private void sendCommand(LutronCommand command) {
        IPBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            this.logger.info("Not sending command, no bridge associated");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR, "No bridge associated");
        } else {
            bridgeHandler.sendCommand(command);
        }
    }

    protected void output(Object... parameters) {
        sendCommand(
                new LutronCommand(LutronOperation.EXECUTE, LutronCommandType.OUTPUT, getIntegrationId(), parameters));
    }

    protected void device(Object... parameters) {
        sendCommand(
                new LutronCommand(LutronOperation.EXECUTE, LutronCommandType.DEVICE, getIntegrationId(), parameters));
    }

    protected void queryOutput(Object... parameters) {
        sendCommand(new LutronCommand(LutronOperation.QUERY, LutronCommandType.OUTPUT, getIntegrationId(), parameters));
    }

    protected void queryDevice(Object... parameters) {
        sendCommand(new LutronCommand(LutronOperation.QUERY, LutronCommandType.DEVICE, getIntegrationId(), parameters));
    }
}
