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
package org.openhab.binding.lametrictime.internal.api.impl;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation class for http icons.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class HTTPIcon extends AbstractDataIcon {
    private final URI uri;

    public HTTPIcon(String uri) {
        this(URI.create(uri));
    }

    public HTTPIcon(URI uri) {
        this.uri = uri;
    }

    @Override
    protected void populateFields() {
        Client client = ClientBuilder.newBuilder().build();
        Response response = client.target(uri).request().get();

        setType(response.getMediaType().toString());
        setData(response.readEntity(byte[].class));
    }
}
