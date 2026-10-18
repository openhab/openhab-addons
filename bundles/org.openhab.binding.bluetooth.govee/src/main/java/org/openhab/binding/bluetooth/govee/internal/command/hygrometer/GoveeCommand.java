/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Connor Petty - Initial Contribution
 *
 */
@NonNullByDefault
public abstract class GoveeCommand {

    public static final byte READ_TYPE = -86;
    public static final byte WRITE_TYPE = 51;

    public abstract byte getCommandType();

    public abstract byte getCommandCode();

    protected abstract byte @Nullable [] getData();

    public GoveeMessage createMessage() {
        return new GoveeMessage(getCommandType(), getCommandCode(), getData());
    }

    public abstract void handleResponse(byte @Nullable [] data, @Nullable Throwable th);

    public boolean matches(GoveeMessage message) {
        return message.getCommandType() == getCommandType() && message.getCommandCode() == getCommandCode();
    }
}
