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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class GetVersionCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetVersionCommand.class);
    private String remoteControllerVersion;
    private String communicationControllerVersion;

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

            int remoteControllerMajor = Integer.valueOf(mm.getValues().get(0x45).getRawValue()[0]);
            int remoteControllerMinor = Integer.valueOf(mm.getValues().get(0x45).getRawValue()[1]);
            int remoteControllerRevision = Integer.valueOf(mm.getValues().get(0x45).getRawValue()[2]);

            this.remoteControllerVersion = remoteControllerMajor + "." + remoteControllerMinor + "."
                    + remoteControllerRevision;

            int commControllerMajor = Integer.valueOf(mm.getValues().get(0x46).getRawValue()[0]);
            int commControllerMinor = Integer.valueOf(mm.getValues().get(0x46).getRawValue()[1]);

            this.communicationControllerVersion = commControllerMajor + "." + commControllerMinor;

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
        return 304;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String getRemoteControllerVersion() {
        return remoteControllerVersion;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public String getCommunicationControllerVersion() {
        return communicationControllerVersion;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
