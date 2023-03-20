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
package org.openhab.binding.dmx.internal.dmxoverethernet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ArtnetNode} represents a sending or receiving node with address and port
 * default address is set to 6454 for ArtNet
 *
 * @author Jan N. Klug - Initial contribution
 *
 */
@NonNullByDefault
public class ArtnetNode extends IpNode {
    public static final int DEFAULT_PORT = 6454;

    /**
     * constructor with address
     *
     * @param addrString address in format address[:port]
     */
    public ArtnetNode(String addrString) {
        super(addrString);
        if (port == 0) {
            port = DEFAULT_PORT;
        }
    }
}
