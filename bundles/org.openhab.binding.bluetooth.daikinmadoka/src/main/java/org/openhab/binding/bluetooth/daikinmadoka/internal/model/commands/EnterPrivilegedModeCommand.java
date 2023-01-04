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
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command enable privileged commands on remote device
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class EnterPrivilegedModeCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(EnterPrivilegedModeCommand.class);

    @Override
    public byte[][] getRequest() {
        MadokaValue privilegedMode = new MadokaValue(0xfe, 1, new byte[] { (byte) 0x01 });
        return MadokaMessage.createRequest(this, privilegedMode);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm) {
        byte[] msg = mm.getRawMessage();
        if (logger.isDebugEnabled() && msg != null) {
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(msg));
        }

        setState(State.SUCCEEDED);
    }

    @Override
    public int getCommandId() {
        return 16658;
    }
}
