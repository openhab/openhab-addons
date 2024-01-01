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
package org.openhab.binding.lametrictime.internal.api.common.impl;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

/**
 * Abstract class for clients.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractClient {
    protected final ClientBuilder clientBuilder;

    @Nullable
    private volatile Client client;
    @Nullable
    private volatile Gson gson;

    public AbstractClient() {
        this(ClientBuilder.newBuilder());
    }

    public AbstractClient(ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    protected @Nullable Client getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = createClient();
                }
            }
        }

        return client;
    }

    protected @Nullable Gson getGson() {
        if (gson == null) {
            synchronized (this) {
                if (gson == null) {
                    gson = createGson();
                }
            }
        }

        return gson;
    }

    protected abstract Client createClient();

    protected Gson createGson() {
        return GsonGenerator.create();
    }

    protected ClientBuilder getClientBuilder() {
        return clientBuilder;
    }
}
