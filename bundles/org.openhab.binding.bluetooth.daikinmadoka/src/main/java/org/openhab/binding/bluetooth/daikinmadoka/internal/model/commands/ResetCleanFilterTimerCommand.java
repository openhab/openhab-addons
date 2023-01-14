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
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;

/**
 * Command used to reset the Clean Filter Indicator timer
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class ResetCleanFilterTimerCommand extends BRC1HCommand {

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        setState(State.SUCCEEDED);
    }

    @Override
    public byte[][] getRequest() {
        MadokaValue mv = new MadokaValue(0xFE, 1, new byte[] { (byte) 0x01 });
        return MadokaMessage.createRequest(this, mv);
    }

    @Override
    public int getCommandId() {
        return 16928;
    }
}
