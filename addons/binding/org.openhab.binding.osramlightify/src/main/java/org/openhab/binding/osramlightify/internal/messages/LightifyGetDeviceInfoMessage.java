/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;

import org.eclipse.smarthome.core.thing.ThingStatus;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.messages.LightifyBaseGetDeviceInfoMessage;

/**
 * Get the state of a single device.
 *
 * @author Mike Jagdis - Initial contribution
 */
public class LightifyGetDeviceInfoMessage extends LightifyBaseGetDeviceInfoMessage implements LightifyMessage {

    private final LightifyDeviceHandler deviceHandler;

    public LightifyGetDeviceInfoMessage(LightifyDeviceHandler deviceHandler) {
        super(deviceHandler);

        this.deviceHandler = deviceHandler;
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        if (super.handleResponse(bridgeHandler, data)) {
            state.received(bridgeHandler, deviceHandler, System.nanoTime(), true);

            return true;
        }

        // The poll failed (device busy?) but we only need to retry if the device is still online.
        // If it has gone offline then the only way it will go back online is if/when the gateway
        // gets updated state for it - which is what we were trying to get to happen.
        return (deviceHandler.getThing().getStatus() != ThingStatus.ONLINE);
    }
}
