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
package org.openhab.binding.sleepiq.internal.api;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents configuration parameters for using {@link SleepIQ}.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class Configuration {
    private String username = "";
    private String password = "";

    private URI baseUri = URI.create("https://api.sleepiq.sleepnumber.com");

    /**
     * Get the username on the account.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username on the account. This should be the username used to
     * register with SleepIQ.
     *
     * @param username
     *            the value to set
     */
    public void setUsername(String username) {
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
    public Configuration withUsername(String username) {
        setUsername(username);
        return this;
    }

    /**
     * Get the password on the account.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password on the account. This should be the password used to
     * register with SleepIQ.
     *
     * @param password
     *            the value to set
     */
    public void setPassword(String password) {
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
    public Configuration withPassword(String password) {
        setPassword(password);
        return this;
    }

    /**
     * Get the base URI of the SleepIQ cloud service.
     *
     * @return the base URI
     */
    public URI getBaseUri() {
        return baseUri;
    }

    /**
     * Set the base URI of the SleepIQ cloud service. It is unlikely that this
     * will need to be changed from its default value.
     *
     * @param baseUri
     *            the value to set
     */
    public void setBaseUri(URI baseUri) {
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
    public Configuration withBaseUri(URI baseUri) {
        setBaseUri(baseUri);
        return this;
    }
}
