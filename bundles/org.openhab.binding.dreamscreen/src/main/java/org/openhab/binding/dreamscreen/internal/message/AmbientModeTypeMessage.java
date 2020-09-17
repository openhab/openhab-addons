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

import static org.openhab.binding.dreamscreen.internal.model.DreamScreenScene.COLOR;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link AmbientModeTypeMessage} handles the Ambient Mode Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class AmbientModeTypeMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x03;
    static final byte COMMAND_LOWER = 0x08;

    protected AmbientModeTypeMessage(final byte[] data) {
        super(data);
    }

    public AmbientModeTypeMessage(final byte group, final byte ambientModeType) {
        super(group, COMMAND_UPPER, COMMAND_LOWER, new byte[] { ambientModeType });
    }

    static boolean matches(final byte[] data) {
        return matches(data, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getAmbientModeType() {
        return this.payload.get(0);
    }

    @Override
    public String toString() {
        final String type = getAmbientModeType() == COLOR.ambientModeType ? "COLOR" : "SCENE";
        return "AmbientModeType " + type;
    }
}
