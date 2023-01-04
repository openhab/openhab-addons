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
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;

/**
 * This command returns the firmware version
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetVersionCommand extends BRC1HCommand {

    private @Nullable String remoteControllerVersion;
    private @Nullable String communicationControllerVersion;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        MadokaValue mValue45 = mm.getValues().get(0x45);
        MadokaValue mValue46 = mm.getValues().get(0x46);
        if (mValue45 == null || mValue46 == null) {
            String message = "version value is null when handling the response";
            setState(State.FAILED);
            throw new MadokaParsingException(message);
        }

        byte[] mv45 = mValue45.getRawValue();
        byte[] mv46 = mValue46.getRawValue();

        if (mv45 == null || mv45.length != 3 || mv46 == null || mv46.length != 2) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect version value");
        }

        int remoteControllerMajor = mv45[0];
        int remoteControllerMinor = mv45[1];
        int remoteControllerRevision = mv45[2];
        this.remoteControllerVersion = remoteControllerMajor + "." + remoteControllerMinor + "."
                + remoteControllerRevision;

        int commControllerMajor = mv46[0];
        int commControllerMinor = mv46[1];
        this.communicationControllerVersion = commControllerMajor + "." + commControllerMinor;

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
        return 304;
    }

    public @Nullable String getRemoteControllerVersion() {
        return remoteControllerVersion;
    }

    public @Nullable String getCommunicationControllerVersion() {
        return communicationControllerVersion;
    }
}
