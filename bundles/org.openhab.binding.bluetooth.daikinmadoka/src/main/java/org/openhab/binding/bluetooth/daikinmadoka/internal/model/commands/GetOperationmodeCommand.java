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

import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OperationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class GetOperationmodeCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetOperationmodeCommand.class);

    private OperationMode operationMode;

    @Override
    public byte[] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm) {
        try {
            operationMode = OperationMode.valueOf(mm.getValues().get(0x20).getRawValue()[0]);

            logger.debug("operationMode: {}", operationMode);

            setState(State.SUCCEEDED);
            executor.execute(() -> listener.receivedResponse(this));

            return true;
        } catch (Exception e) {
            logger.debug("Error while parsing response", e);
            setState(State.FAILED);
        }
        return false;
    }

    @Override
    public int getCommandId() {
        return 48;
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

}
