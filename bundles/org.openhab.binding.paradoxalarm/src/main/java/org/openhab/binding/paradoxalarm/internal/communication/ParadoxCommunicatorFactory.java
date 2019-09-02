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
import java.util.concurrent.ScheduledExecutorService;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxCommunicatorFactory} used to create the proper communication implementation objects based on panel
 * type.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxCommunicatorFactory {

    private final Logger logger = LoggerFactory.getLogger(ParadoxCommunicatorFactory.class);

    private String ipAddress;
    private int tcpPort;
    private String ip150Password;
    private String pcPassword;
    private ScheduledExecutorService scheduler;

    public ParadoxCommunicatorFactory(String ipAddress, int tcpPort, String ip150Password, String pcPassword,
            ScheduledExecutorService scheduler) {
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.ip150Password = ip150Password;
        this.pcPassword = pcPassword;
        this.scheduler = scheduler;
    }

    public IParadoxCommunicator createCommunicator(String panelTypeStr) {
        PanelType panelType = PanelType.from(panelTypeStr);
        try {
            return createCommunicator(panelType);
        } catch (IOException | ParadoxException e) {
            logger.warn("Unable to create communicator for Panel {}. Exception={}", panelTypeStr, e.getMessage());
            return null;
        }
    }

    public IParadoxCommunicator createCommunicator(PanelType panelType)
            throws UnknownHostException, IOException, ParadoxException {
        switch (panelType) {
            case EVO48:
            case EVO192:
            case EVOHD:
                logger.debug("Creating new communicator for Paradox {} system", panelType);
                return new EvoCommunicator(ipAddress, tcpPort, ip150Password, pcPassword, scheduler, panelType);
            default:
                throw new ParadoxException("Unsupported panel type: " + panelType);
        }
    }
}
