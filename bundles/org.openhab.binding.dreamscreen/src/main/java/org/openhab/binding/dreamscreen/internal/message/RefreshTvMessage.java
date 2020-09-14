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
package org.openhab.binding.dreamscreen.internal.message;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreen4kHandler;
import org.openhab.binding.dreamscreen.internal.handler.DreamScreenHdHandler;

/**
 * {@link RefreshTvMessage} handles the Refresh TV Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class RefreshTvMessage extends RefreshMessage {

    protected RefreshTvMessage(final byte[] data, final int off) {
        super(data, off);
    }

    static boolean matches(final byte[] data, final int off) {
        if (RefreshMessage.matches(data, off)) {
            final int msgLen = data[off + 1] & 0xFF;
            final byte productId = data[off + msgLen];
            return productId == DreamScreenHdHandler.PRODUCT_ID || productId == DreamScreen4kHandler.PRODUCT_ID;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TV Refresh";
    }

    public byte getInput() {
        return this.payload.get(73);
    }

    public String getInputName1() {
        return new String(this.payload.array(), 75, 16, StandardCharsets.UTF_8).trim();
    }

    public String getInputName2() {
        return new String(this.payload.array(), 91, 16, StandardCharsets.UTF_8).trim();
    }

    public String getInputName3() {
        return new String(this.payload.array(), 107, 16, StandardCharsets.UTF_8).trim();
    }
}
