/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.messages;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyException;
import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * Set an effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifySetEffectMessage extends LightifyBaseMessage implements LightifyMessage {

    private String name;
    private String params;
    private byte[] data;
    private boolean background = false;

    public LightifySetEffectMessage(LightifyDeviceHandler deviceHandler, String name, String params, boolean color, byte[] data) {
        this(deviceHandler, name, params, color, data, false);
    }

    public LightifySetEffectMessage(LightifyDeviceHandler deviceHandler, String name, String params, boolean color, byte[] data, boolean background) {
        super(deviceHandler, (color ? Command.SET_EFFECT_COLOR : Command.SET_EFFECT_WHITE));

        this.name = name;
        this.params = params;
        this.data = data;
        this.background = background;
    }

    public boolean isBackground() {
        return background;
    }

    @Override
    public String toString() {
        return super.toString()
            + ", effect = " + name
            + ": " + params;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(9 + 15 * 4)
            .put(data);
    }

    // ****************************************
    //        Response handling section
    // ****************************************

    @Override
    public boolean handleResponse(LightifyBridgeHandler bridgeHandler, ByteBuffer data) throws LightifyException {
        super.handleResponse(bridgeHandler, data);

        return true;
    }
}
