/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
