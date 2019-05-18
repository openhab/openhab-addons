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
package org.openhab.binding.somfymylink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SomfyMyLinkConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfyMyLinkConfiguration {

    @Nullable
    public String ipAddress;

    @Nullable
    public String systemId;

    /*
     * public boolean sameConnectionParameters(IPBridgeConfig config) {
     * return StringUtils.equals(ipAddress, config.ipAddress) && StringUtils.equals(user, config.user)
     * && StringUtils.equals(password, config.password) && (reconnect == config.reconnect)
     * && (heartbeat == config.heartbeat);
     * }
     */
}
