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
package org.openhab.binding.vdr.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VDRConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class VDRConfiguration {

    private String host = "localhost";
    private int port = 6419;
    private Integer refresh = 60;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getRefresh() {
        return refresh;
    }

    public void setRefresh(Integer refresh) {
        this.refresh = refresh;
    }
}
