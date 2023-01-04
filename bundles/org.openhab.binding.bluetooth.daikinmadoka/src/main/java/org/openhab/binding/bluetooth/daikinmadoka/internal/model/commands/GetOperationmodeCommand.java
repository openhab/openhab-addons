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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OperationMode;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command returns the current AC operation mode
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetOperationmodeCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetOperationmodeCommand.class);

    private @Nullable OperationMode operationMode;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        @Nullable
        MadokaValue mode = mm.getValues().get(0x20);
        if (mode == null) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect operation mode");
        }
        byte[] bOperationMode = mode.getRawValue();
        if (bOperationMode == null) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect operation mode");
        }

        operationMode = OperationMode.valueOf(bOperationMode[0]);

        logger.debug("operationMode: {}", operationMode);

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
        return 48;
    }

    public @Nullable OperationMode getOperationMode() {
        return operationMode;
    }
}
