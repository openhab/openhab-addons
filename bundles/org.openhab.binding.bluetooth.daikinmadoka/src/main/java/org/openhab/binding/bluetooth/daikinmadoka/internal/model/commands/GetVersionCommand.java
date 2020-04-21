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

    @Override
    public byte[] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm) {
        try {
            byte[] mv45 = mm.getValues().get(0x45).getRawValue();

            int remoteControllerMajor = Integer.valueOf(mv45[0]);
            int remoteControllerMinor = Integer.valueOf(mv45[1]);
            int remoteControllerRevision = Integer.valueOf(mv45[2]);

            this.remoteControllerVersion = remoteControllerMajor + "." + remoteControllerMinor + "."
                    + remoteControllerRevision;

            byte[] mv46 = mm.getValues().get(0x46).getRawValue();

            int commControllerMajor = Integer.valueOf(mv46[0]);
            int commControllerMinor = Integer.valueOf(mv46[1]);

            this.communicationControllerVersion = commControllerMajor + "." + commControllerMinor;

            setState(State.SUCCEEDED);
            executor.execute(() -> listener.receivedResponse(this));

            return true;
        } catch (Exception e) {
            logger.debug("Error while parsing response to GetVersion command", e);
            setState(State.FAILED);
        }
        return false;
    }

    @Override
    public int getCommandId() {
        return 304;
    }

    public String getRemoteControllerVersion() {
        return remoteControllerVersion;
    }

    public String getCommunicationControllerVersion() {
        return communicationControllerVersion;
    }

}
