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
package org.openhab.binding.avmfritz.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bean holding configuration data for FRITZ! Box.
 *
 * @author Robert Bausdorf - Initial contribution
 */
@NonNullByDefault
public class AVMFritzBoxConfiguration {

    public @NonNullByDefault({}) String ipAddress;
    public @Nullable Integer port;
    public String protocol = "http";

    public @Nullable String user;
    public @NonNullByDefault({}) String password;

    public long pollingInterval = 15;
    public long asyncTimeout = 10000;
    public long syncTimeout = 2000;

    @Override
    public String toString() {
        return new StringBuilder().append("[IP=").append(ipAddress).append(",port=").append(port).append(",protocol=")
                .append(protocol).append(",user=").append(user).append(",pollingInterval=").append(pollingInterval)
                .append(",asyncTimeout=").append(asyncTimeout).append(",syncTimeout=").append(syncTimeout).append("]")
                .toString();
    }
}
