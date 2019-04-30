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

import org.eclipse.smarthome.core.library.types.DecimalType;

import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * Activate a scene defined on a Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyActivateSceneMessage extends LightifyBaseMessage implements LightifyMessage {

    private byte sceneNo;

    public LightifyActivateSceneMessage(DecimalType sceneNo) {
        super(null, Command.ACTIVATE_SCENE);

        this.sceneNo = (byte) (sceneNo.intValue() & 0xff);
    }

    @Override
    public String toString() {
        return super.toString() + ", scene " + sceneNo;
    }

    // ****************************************
    //      Request transmission section
    // ****************************************

    @Override
    public ByteBuffer encodeMessage() throws LightifyMessageTooLongException {
        return super.encodeMessage(1)
            .put(sceneNo);
    }

    // ****************************************
    //        Response handling section
    // ****************************************
}
