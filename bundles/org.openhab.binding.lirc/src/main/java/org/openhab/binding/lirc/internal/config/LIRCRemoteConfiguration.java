/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lirc.internal.config;

/**
 * Configuration class for LIRCRemote device.
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCRemoteConfiguration {

    private String remote;

    /**
     * @return the remote
     */
    public String getRemote() {
        return remote;
    }

    /**
     * @param remote the name of the remote
     */
    public void setRemote(String remote) {
        this.remote = remote;
    }
}
