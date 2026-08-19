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
package org.openhab.binding.tidal.internal.api.model;

import com.google.gson.annotations.JsonAdapter;

/**
 * Tidal Api Track data class.
 *
 * @author Laurent Arnal - Initial contribution
 */

@JsonAdapter(FlatteningTypeAdapterFactory.class)
public class Session {
    private String sessionId;
    private String userId;
    private Client client;

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return client.id;
    }

    private class Client {
        private String id;
    }
}
