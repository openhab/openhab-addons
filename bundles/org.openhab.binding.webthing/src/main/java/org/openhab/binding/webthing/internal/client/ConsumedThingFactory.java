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
package org.openhab.binding.webthing.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.IOException;
import java.net.URI;

/**
 * Factory to create new instances of the WebThing client-side proxy
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public interface ConsumedThingFactory {

    /**
     *
     * @param webThingURI  the identifier of a WebThing resource
     * @param connectionListener  the connection listener to observe the connection state of the WebThing connection
     * @return the newly created WebThing
     * @throws IOException if the WebThing can not be connected
     */
    ConsumedThing create(URI webThingURI, ConnectionListener connectionListener) throws IOException;

    /**
     * @return the default instance of the factory
     */
    static ConsumedThingFactory instance() {
        return ConsumedThingImpl::new;
    }
}