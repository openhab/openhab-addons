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
package org.openhab.binding.lgtvserial.internal.protocol.serial.responses;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * This class represents a percentage response.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class QuantityResponse implements LGSerialResponse {

    private int setId;

    private boolean success;

    private State state;

    public QuantityResponse(int setId, boolean success, QuantityType<?> state) {
        this.setId = setId;
        this.success = success;
        this.state = state;
    }

    @Override
    public int getSetID() {
        return setId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
