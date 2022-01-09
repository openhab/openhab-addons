/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link GetBatteryLevelCommand} is used to get the current battery level of the AM43.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GetBatteryLevelCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0xa2;

    public GetBatteryLevelCommand() {
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

    public int getBatteryLevel() {
        return getResponse()[7];
    }

    @Override
    public int minResponseSize() {
        return 9;
    }
}
