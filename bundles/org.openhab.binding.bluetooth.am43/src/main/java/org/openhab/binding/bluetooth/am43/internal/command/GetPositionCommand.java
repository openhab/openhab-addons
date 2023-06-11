/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.am43.internal.command;

import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GetPositionCommand} is used to get the current position of the blinds, ranging from 0 to 100.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GetPositionCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0xa1;

    public GetPositionCommand() {
        super(COMMAND, (byte) 1);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {
        if (super.handleResponse(executor, listener, response)) {
            executor.execute(() -> listener.receivedResponse(this));
            return true;
        }
        return false;
    }

    public int getPosition() {
        return getResponse()[4];
    }

    @Override
    public int minResponseSize() {
        return 6;
    }
}
