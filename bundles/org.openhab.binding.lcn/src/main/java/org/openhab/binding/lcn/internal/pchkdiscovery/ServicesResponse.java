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
package org.openhab.binding.lcn.internal.pchkdiscovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Used for deserializing the XML response of the LCN-PCHK discovery protocol.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class ServicesResponse {
    @XStreamAlias("Version")
    private final Version version;
    @XStreamAlias("Server")
    private final Server server;
    @XStreamAlias("ExtServices")
    private final ExtServices extServices;
    @XStreamAlias("Services")
    private final Object services = new Object();

    public ServicesResponse(Version version, Server server, ExtServices extServices) {
        this.version = version;
        this.server = server;
        this.extServices = extServices;
    }

    public Server getServer() {
        return server;
    }

    public Version getVersion() {
        return version;
    }

    public ExtServices getExtServices() {
        return extServices;
    }
}
