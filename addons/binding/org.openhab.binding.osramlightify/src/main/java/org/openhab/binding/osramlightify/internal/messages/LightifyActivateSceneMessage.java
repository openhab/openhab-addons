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

import org.eclipse.smarthome.core.library.types.DecimalType;

import org.openhab.binding.osramlightify.internal.exceptions.LightifyMessageTooLongException;

/**
 * Activate a scene defined on a Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
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
