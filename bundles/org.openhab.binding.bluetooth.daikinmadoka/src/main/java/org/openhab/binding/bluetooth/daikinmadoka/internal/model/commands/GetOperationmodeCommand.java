/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OPERATION_MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class GetOperationmodeCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetOperationmodeCommand.class);

    private OPERATION_MODE operationMode;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public byte[] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {
        if (response == null) {
            return false;
        }

        try {

            MadokaMessage mm = MadokaMessage.parse(response);

            operationMode = OPERATION_MODE.valueOf(mm.getValues().get(0x20).getRawValue()[0]);

            logger.debug("operationMode: {}", operationMode);

            listener.receivedResponse(this);
            setState(State.SUCCEEDED);
            return true;
        } catch (Exception e) {
            logger.error("Error while parsing response", e);
            setState(State.FAILED);
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getCommandId() {
        return 48;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public OPERATION_MODE getOperationMode() {
        return operationMode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
