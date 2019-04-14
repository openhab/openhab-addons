/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;
import java.net.UnknownHostException;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxCommunicatorFactory} used to create the proper communication implementatino objects based on panel
 * type.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxCommunicatorFactory {

    private final Logger logger = LoggerFactory.getLogger(ParadoxCommunicatorFactory.class);

    private String ipAddress;
    private int tcpPort;
    private String ip150Password;
    private String pcPassword;

    public ParadoxCommunicatorFactory(String ipAddress, int tcpPort, String ip150Password, String pcPassword) {
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.ip150Password = ip150Password;
        this.pcPassword = pcPassword;
    }

    public IParadoxCommunicator createCommunicator(String panelTypeStr)
            throws UnknownHostException, IOException, InterruptedException, ParadoxBindingException {
        PanelType panelType = PanelType.from(panelTypeStr);
        return createCommunicator(panelType);
    }

    public IParadoxCommunicator createCommunicator(PanelType panelType)
            throws UnknownHostException, IOException, InterruptedException, ParadoxBindingException {
        switch (panelType) {
            case EVO48:
            case EVO192:
            case EVOHD:
                logger.debug("Creating new communicator for Paradox {} system", panelType);
                return new EvoCommunicator(ipAddress, tcpPort, ip150Password, pcPassword);
            default:
                throw new ParadoxBindingException("Unsupported panel type: " + panelType);
        }
    }
}
