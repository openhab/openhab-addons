/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial.responses;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

/**
 * This class represents a decimal response.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class DecimalResponse implements LGSerialResponse {

    private int setId;

    private boolean success;

    private State state;

    public DecimalResponse(int setId, boolean success, DecimalType state) {
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
