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
package org.openhab.binding.lametrictime.api.cloud;

import java.net.URI;

public class CloudConfiguration
{
    private URI baseUri = URI.create("https://developer.lametric.com/api/v2");

    private boolean logging = false;
    private String logLevel = "INFO";
    private int logMax = 104857600; // 100kb

    public URI getBaseUri()
    {
        return baseUri;
    }

    public void setBaseUri(URI baseUri)
    {
        this.baseUri = baseUri;
    }

    public CloudConfiguration withBaseUri(URI baseUri)
    {
        this.baseUri = baseUri;
        return this;
    }

    public boolean isLogging()
    {
        return logging;
    }

    public void setLogging(boolean logging)
    {
        this.logging = logging;
    }

    public CloudConfiguration withLogging(boolean logging)
    {
        this.logging = logging;
        return this;
    }

    public String getLogLevel()
    {
        return logLevel;
    }

    public void setLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
    }

    public CloudConfiguration withLogLevel(String logLevel)
    {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogMax()
    {
        return logMax;
    }

    public void setLogMax(int logMax)
    {
        this.logMax = logMax;
    }

    public CloudConfiguration withLogMax(int logMax)
    {
        this.logMax = logMax;
        return this;
    }
}
