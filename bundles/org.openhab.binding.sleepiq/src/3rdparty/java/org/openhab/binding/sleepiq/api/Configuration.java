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
package org.openhab.binding.sleepiq.api;

import java.net.URI;
import java.util.logging.Level;

/**
 * This class represents configuration parameters for using {@link SleepIQ}.
 *
 * @author Gregory Moyer
 */
public class Configuration
{
    private String username;
    private String password;

    private URI baseUri = URI.create("https://api.sleepiq.sleepnumber.com/rest");

    private boolean logging = false;

    /**
     * Get the username on the account.
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Set the username on the account. This should be the username used to
     * register with SleepIQ.
     *
     * @param username
     *            the value to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Set the username on the account. This should be the username used to
     * register with SleepIQ.
     *
     * @param username
     *            the value to set
     * @return this configuration instance
     */
    public Configuration withUsername(String username)
    {
        setUsername(username);
        return this;
    }

    /**
     * Get the password on the account.
     *
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Set the password on the account. This should be the password used to
     * register with SleepIQ.
     *
     * @param password
     *            the value to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Set the password on the account. This should be the password used to
     * register with SleepIQ.
     *
     * @param password
     *            the value to set
     * @return this configuration instance
     */
    public Configuration withPassword(String password)
    {
        setPassword(password);
        return this;
    }

    /**
     * Get the base URI of the SleepIQ cloud service.
     *
     * @return the base URI
     */
    public URI getBaseUri()
    {
        return baseUri;
    }

    /**
     * Set the base URI of the SleepIQ cloud service. It is unlikely that this
     * will need to be changed from its default value.
     *
     * @param baseUri
     *            the value to set
     */
    public void setBaseUri(URI baseUri)
    {
        this.baseUri = baseUri;
    }

    /**
     * Set the base URI of the SleepIQ cloud service. It is unlikely that this
     * will need to be changed from its default value.
     *
     * @param baseUri
     *            the value to set
     * @return this configuration instance
     */
    public Configuration withBaseUri(URI baseUri)
    {
        setBaseUri(baseUri);
        return this;
    }

    /**
     * Get the logging flag.
     *
     * @return the logging flag
     */
    public boolean isLogging()
    {
        return logging;
    }

    /**
     * Set the logging flag. When this is set to <code>true</code>, all requests
     * and responses will be logged at the {@link Level#INFO} level. <b>This
     * includes usernames and passwords!</b>
     *
     * @param logging
     *            the value to set
     */
    public void setLogging(boolean logging)
    {
        this.logging = logging;
    }

    /**
     * Set the logging flag. When this is set to <code>true</code>, all requests
     * and responses will be logged at the {@link Level#INFO} level. <b>This
     * includes usernames and passwords!</b>
     *
     * @param logging
     *            the value to set
     * @return this configuration instance
     */
    public Configuration withLogging(boolean logging)
    {
        setLogging(logging);
        return this;
    }
}
