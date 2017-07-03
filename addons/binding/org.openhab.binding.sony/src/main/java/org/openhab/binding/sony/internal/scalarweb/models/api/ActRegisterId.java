/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 *
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.openhab.binding.sony.internal.net.NetUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class ActRegisterId.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ActRegisterId {

    /** The clientid. */
    private final String clientid;

    /** The nickname. */
    private final String nickname;

    /**
     * Instantiates a new act register id.
     */
    public ActRegisterId() {
        clientid = NetUtilities.getDeviceId();
        nickname = NetUtilities.getDeviceName(clientid);
    }

    /**
     * Gets the clientid.
     *
     * @return the clientid
     */
    public String getClientid() {
        return clientid;
    }

    /**
     * Gets the nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ActRegisterId [clientid=" + clientid + ", nickname=" + nickname + "]";
    }
}
