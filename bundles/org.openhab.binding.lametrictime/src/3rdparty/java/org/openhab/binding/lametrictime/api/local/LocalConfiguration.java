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
package org.openhab.binding.lametrictime.api.local;

import java.net.URI;
import java.net.URISyntaxException;

public class LocalConfiguration
{
    private String host;
    private String apiKey;

    private boolean secure = true;
    private boolean ignoreCertificateValidation = true;
    private boolean ignoreHostnameValidation = true;

    private String insecureScheme = "http";
    private int insecurePort = 8080;

    private String secureScheme = "https";
    private int securePort = 4343;

    private String basePath = "/api/v2";

    private String authUser = "dev";

    private boolean logging = false;
    private String logLevel = "INFO";
    private int logMax = 104857600; // 100kb

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public LocalConfiguration withHost(String host)
    {
        this.host = host;
        return this;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public LocalConfiguration withApiKey(String apiKey)
    {
        this.apiKey = apiKey;
        return this;
    }

    public boolean isSecure()
    {
        return secure;
    }

    public void setSecure(boolean secure)
    {
        this.secure = secure;
    }

    public LocalConfiguration withSecure(boolean secure)
    {
        this.secure = secure;
        return this;
    }

    public boolean isIgnoreCertificateValidation()
    {
        return ignoreCertificateValidation;
    }

    public void setIgnoreCertificateValidation(boolean ignoreCertificateValidation)
    {
        this.ignoreCertificateValidation = ignoreCertificateValidation;
    }

    public LocalConfiguration withIgnoreCertificateValidation(boolean ignoreCertificateValidation)
    {
        this.ignoreCertificateValidation = ignoreCertificateValidation;
        return this;
    }

    public boolean isIgnoreHostnameValidation()
    {
        return ignoreHostnameValidation;
    }

    public void setIgnoreHostnameValidation(boolean ignoreHostnameValidation)
    {
        this.ignoreHostnameValidation = ignoreHostnameValidation;
    }

    public LocalConfiguration withIgnoreHostnameValidation(boolean ignoreHostnameValidation)
    {
        this.ignoreHostnameValidation = ignoreHostnameValidation;
        return this;
    }

    public String getInsecureScheme()
    {
        return insecureScheme;
    }

    public void setInsecureScheme(String insecureScheme)
    {
        this.insecureScheme = insecureScheme;
    }

    public LocalConfiguration withInsecureScheme(String insecureScheme)
    {
        this.insecureScheme = insecureScheme;
        return this;
    }

    public int getInsecurePort()
    {
        return insecurePort;
    }

    public void setInsecurePort(int insecurePort)
    {
        this.insecurePort = insecurePort;
    }

    public LocalConfiguration withInsecurePort(int insecurePort)
    {
        this.insecurePort = insecurePort;
        return this;
    }

    public String getSecureScheme()
    {
        return secureScheme;
    }

    public void setSecureScheme(String secureScheme)
    {
        this.secureScheme = secureScheme;
    }

    public LocalConfiguration withSecureScheme(String secureScheme)
    {
        this.secureScheme = secureScheme;
        return this;
    }

    public int getSecurePort()
    {
        return securePort;
    }

    public void setSecurePort(int securePort)
    {
        this.securePort = securePort;
    }

    public LocalConfiguration withSecurePort(int securePort)
    {
        this.securePort = securePort;
        return this;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public LocalConfiguration withBasePath(String basePath)
    {
        this.basePath = basePath;
        return this;
    }

    public String getAuthUser()
    {
        return authUser;
    }

    public void setAuthUser(String authUser)
    {
        this.authUser = authUser;
    }

    public LocalConfiguration withAuthUser(String authUser)
    {
        this.authUser = authUser;
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

    public LocalConfiguration withLogging(boolean logging)
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

    public LocalConfiguration withLogLevel(String logLevel)
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

    public LocalConfiguration withLogMax(int logMax)
    {
        this.logMax = logMax;
        return this;
    }

    public URI getBaseUri()
    {
        String scheme = secure ? secureScheme : insecureScheme;
        int port = secure ? securePort : insecurePort;
        try
        {
            return new URI(scheme, null, host, port, basePath, null, null);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Invalid configuration", e);
        }
    }
}
