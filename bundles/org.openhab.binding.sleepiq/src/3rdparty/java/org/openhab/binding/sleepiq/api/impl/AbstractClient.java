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
package org.openhab.binding.sleepiq.api.impl;

import javax.ws.rs.client.Client;

import com.google.gson.Gson;

public abstract class AbstractClient
{
    private volatile Client client;
    private volatile Gson gson;

    protected Client getClient()
    {
        if (client == null)
        {
            synchronized (this)
            {
                if (client == null)
                {
                    client = createClient();
                }
            }
        }

        return client;
    }

    protected Gson getGson()
    {
        if (gson == null)
        {
            synchronized (this)
            {
                if (gson == null)
                {
                    gson = createGson();
                }
            }
        }

        return gson;
    }

    protected abstract Client createClient();

    protected Gson createGson()
    {
        return GsonGenerator.create();
    }
}
