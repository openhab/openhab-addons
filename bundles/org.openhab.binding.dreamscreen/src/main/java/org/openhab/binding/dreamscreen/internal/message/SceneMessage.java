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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreamscreen.internal.model.DreamScreenScene;

/**
 * {@link SceneMessage} handles the Scene Message.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class SceneMessage extends DreamScreenMessage {
    static final byte COMMAND_UPPER = 0x03;
    static final byte COMMAND_LOWER = 0x0D;

    protected SceneMessage(final byte[] data, final int off) {
        super(data, off);
    }

    public SceneMessage(byte group, byte ambientScene) {
        super(group, COMMAND_UPPER, COMMAND_LOWER, new byte[] { ambientScene });
    }

    static boolean matches(final byte[] data, final int off) {
        return matches(data, off, COMMAND_UPPER, COMMAND_LOWER);
    }

    public byte getScene() {
        // TODO Auto-generated method stub
        return this.payload.get(0);
    }

    @Override
    public String toString() {
        return "Scene " + DreamScreenScene.fromDeviceScene(getScene()).name();
    }
}
