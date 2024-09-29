/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.govee.internal.command.hygrometer;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.gattserial.SimpleMessageServicer;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class GoveeCommand implements SimpleMessageServicer<GoveeMessage> {

    public static final byte READ_TYPE = -86;
    public static final byte WRITE_TYPE = 51;

    public abstract byte getCommandType();

    public abstract byte getCommandCode();

    protected abstract byte @Nullable [] getData();

    @Override
    public long getTimeout(TimeUnit unit) {
        return unit.convert(60, TimeUnit.SECONDS);
    }

    @Override
    public GoveeMessage createMessage() {
        return new GoveeMessage(getCommandType(), getCommandCode(), getData());
    }

    @Override
    public boolean handleFailedMessage(GoveeMessage message, Throwable th) {
        if (matches(message)) {
            handleResponse(null, th);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleReceivedMessage(GoveeMessage message) {
        if (matches(message)) {
            handleResponse(message.getData(), null);
            return true;
        }
        return false;
    }

    public abstract void handleResponse(byte @Nullable [] data, @Nullable Throwable th);

    protected boolean matches(GoveeMessage message) {
        return message.getCommandType() == getCommandType() && message.getCommandCode() == getCommandCode();
    }
}
