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

/**
 * Command used to get the Clean Filter Indicator status
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetCleanFilterIndicatorCommand extends BRC1HCommand {

    private @Nullable Boolean cleanFilterIndicator;

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        MadokaValue mValue = mm.getValues().get(0x62);
        if (mValue == null) {
            String message = "clean filter indicator is null when handling the response";
            setState(State.FAILED);
            throw new MadokaParsingException(message);
        }

        byte[] valueCleanFilterIndicator = mValue.getRawValue();
        if (valueCleanFilterIndicator == null || valueCleanFilterIndicator.length != 1) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect clean filter indicator value");
        }

        if ((valueCleanFilterIndicator[0] & 0x01) == 0x01) {
            this.cleanFilterIndicator = true;
        } else {
            this.cleanFilterIndicator = false;
        }

        setState(State.SUCCEEDED);
        try {
            executor.execute(() -> listener.receivedResponse(this));
        } catch (Exception e) {
            setState(State.FAILED);
            throw new MadokaParsingException(e);
        }
    }

    public @Nullable Boolean getCleanFilterIndicator() {
        return cleanFilterIndicator;
    }

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public int getCommandId() {
        return 256;
    }
}
