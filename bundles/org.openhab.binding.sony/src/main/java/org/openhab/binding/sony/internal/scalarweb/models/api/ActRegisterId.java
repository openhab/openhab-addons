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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.net.NetUtil;

/**
 * The class represents the active registration and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ActRegisterId {

    /** The clientid for registration */
    private final String clientid = NetUtil.getDeviceId();

    /** The nickname for registration */
    private final String nickname = NetUtil.getDeviceName();

    /**
     * Gets the clientid for registration
     *
     * @return the clientid for registration
     */
    public String getClientid() {
        return clientid;
    }

    /**
     * Gets the nickname for registration
     *
     * @return the nickname for registration
     */
    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "ActRegisterId [clientid=" + clientid + ", nickname=" + nickname + "]";
    }
}
