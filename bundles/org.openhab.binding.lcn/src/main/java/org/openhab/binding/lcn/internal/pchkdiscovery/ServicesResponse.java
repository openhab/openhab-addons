/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used for deserializing the XML response of the LCN-PCHK discovery protocol.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class ServicesResponse {
    private final Version Version;
    private final Server Server;
    private final ExtServices ExtServices;
    @SuppressWarnings("unused")
    private final Object Services = new Object();

    public ServicesResponse(Version version, Server server, ExtServices extServices) {
        this.Version = version;
        this.Server = server;
        this.ExtServices = extServices;
    }

    public Server getServer() {
        return Server;
    }

    public Version getVersion() {
        return Version;
    }

    public ExtServices getExtServices() {
        return ExtServices;
    }
}
