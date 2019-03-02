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
package org.openhab.binding.net.internal.config;

import org.openhab.binding.net.internal.handler.UdpServerHandler;

/**
 * Configuration class for {@link UdpServerHandler }.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class UdpServerConfiguration {
    public int port;
    public int maxpdu;
    public String charset;

    @Override
    public String toString() {
        return "[" + "port=" + port + ", maxpdu=" + maxpdu + ", charset=" + charset + "]";
    }
}
