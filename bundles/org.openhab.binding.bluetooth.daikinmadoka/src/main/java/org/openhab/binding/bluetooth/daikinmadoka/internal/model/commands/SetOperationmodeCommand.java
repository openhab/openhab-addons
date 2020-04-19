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
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OPERATION_MODE;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class SetOperationmodeCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(SetOperationmodeCommand.class);

    private OPERATION_MODE operationMode;

    public SetOperationmodeCommand(OPERATION_MODE operationMode) {
        this.operationMode = operationMode;
    }

    @Override
    public byte[] getRequest() {
        MadokaValue mv = new MadokaValue(0x20, 1, new byte[] { (byte) this.operationMode.value() });
        return MadokaMessage.createRequest(this, mv);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {

        if (response == null) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(response));
        }

        listener.receivedResponse(this);
        setState(State.SUCCEEDED);

        return true;
    }

    @Override
    public int getCommandId() {
        return 16432;
    }

    public OPERATION_MODE getOperationMode() {
        return operationMode;
    }

}
