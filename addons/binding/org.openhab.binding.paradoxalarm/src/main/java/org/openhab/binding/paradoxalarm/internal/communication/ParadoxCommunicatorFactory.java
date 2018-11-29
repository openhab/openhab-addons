/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication;

import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxBindingException;
import org.openhab.binding.paradoxalarm.internal.model.PanelType;

/**
 * The {@link ParadoxCommunicatorFactory} used to create the proper communication implementatino objects based on panel
 * type.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxCommunicatorFactory {

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

    public IParadoxCommunicator createCommunicator(PanelType panelType) throws Exception {
        switch (panelType) {
            case EVO48:
            case EVO192:
            case EVOHD:
                return new EvoCommunicator(ipAddress, tcpPort, ip150Password, pcPassword);
            default:
                throw new ParadoxBindingException("Unsupported panel type: " + panelType);
        }
    }
}
