/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
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
package org.openhab.binding.lametrictime.api.impl;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

public class HTTPIcon extends AbstractDataIcon
{
    private final URI uri;

    public HTTPIcon(String uri)
    {
        this(URI.create(uri));
    }

    public HTTPIcon(URI uri)
    {
        this.uri = uri;
    }

    @Override
    protected void populateFields()
    {
        Client client = ClientBuilder.newBuilder().build();
        Response response = client.target(uri).request().get();

        setType(response.getMediaType().toString());
        setData(response.readEntity(byte[].class));
    }
}
