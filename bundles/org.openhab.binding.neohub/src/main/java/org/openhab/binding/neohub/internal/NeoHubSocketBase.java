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
package org.openhab.binding.neohub.internal;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base abstract class for ASCII based communication between openHAB and NeoHub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public abstract class NeoHubSocketBase implements Closeable {

    protected final NeoHubConfiguration config;

    public NeoHubSocketBase(NeoHubConfiguration config) {
        this.config = config;
    }

    /**
     * Sends the message over the network to the NeoHub and returns its response
     *
     * @param requestJson the message to be sent to the NeoHub
     * @return responseJson received from NeoHub
     * @throws NeoHubException, IOException
     *
     */
    public abstract String sendMessage(final String requestJson) throws IOException, NeoHubException;
}
