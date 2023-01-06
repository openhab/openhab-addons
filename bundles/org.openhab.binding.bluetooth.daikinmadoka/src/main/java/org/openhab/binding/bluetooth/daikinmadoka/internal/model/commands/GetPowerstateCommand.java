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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command returns the current AC power state (on or off)
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetPowerstateCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetPowerstateCommand.class);

    private @Nullable Boolean powerState;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        MadokaValue mValue = mm.getValues().get(0x20);
        if (mValue == null) {
            String message = "powerstate is null when handling the response";
            setState(State.FAILED);
            throw new MadokaParsingException(message);
        }

        byte[] powerStateValue = mValue.getRawValue();
        if (powerStateValue == null || powerStateValue.length != 1) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect value for PowerState");
        }

        powerState = Integer.valueOf(powerStateValue[0]) == 1;

        logger.debug("PowerState: {}", powerState);

        setState(State.SUCCEEDED);
        try {
            executor.execute(() -> listener.receivedResponse(this));
        } catch (Exception e) {
            setState(State.FAILED);
            throw new MadokaParsingException(e);
        }
    }

    @Override
    public int getCommandId() {
        return 32;
    }

    public @Nullable Boolean isPowerState() {
        return powerState;
    }
}
