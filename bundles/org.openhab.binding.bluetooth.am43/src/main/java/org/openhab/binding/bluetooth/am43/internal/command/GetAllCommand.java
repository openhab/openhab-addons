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
import org.openhab.binding.bluetooth.am43.internal.data.Direction;
import org.openhab.binding.bluetooth.am43.internal.data.OperationMode;

/**
 * The {@link GetAllCommand} gets the bulk of the settings on AM43.
 * A GetAllCommand request actually returns several responses. But we are
 * only interested in the device settings response (which luckily has the same command header as the request).
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class GetAllCommand extends AM43Command {

    private static final byte COMMAND = (byte) 0xa7;

    public GetAllCommand() {
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

    public Direction getDirection() {
        return Direction.valueOf((getResponse()[3] & 1) > 0);
    }

    public OperationMode getOperationMode() {
        return OperationMode.valueOf((getResponse()[3] & 2) > 0);
    }

    public boolean getTopLimitSet() {
        return (getResponse()[3] & 4) > 0;
    }

    public boolean getBottomLimitSet() {
        return (getResponse()[3] & 8) > 0;
    }

    public boolean getHasLightSensor() {
        return (getResponse()[3] & 16) > 0;
    }

    public int getSpeed() {
        return getResponse()[4];
    }

    public int getPosition() {
        return getResponse()[5];
    }

    public int getLength() {
        return getResponse()[6] << 8 | getResponse()[7];
    }

    public int getDiameter() {
        return getResponse()[8];
    }

    public int getType() {
        return Math.abs(getResponse()[9] >> 4);
    }

    @Override
    public int minResponseSize() {
        return 10;
    }
}
