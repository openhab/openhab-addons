/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Holds the configuration for the thing.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxConfiguration {

    public static final int PORT = 10000;

    private String ipAddress;
    private String username;
    private String password;
    private String url;

    /**
     * Returns the IP address of the thing.
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address of the thing.
     *
     * @param The ipAddress IP address in format xxx.xxx.xxx.xxx
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the username of the thing.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the thing.
     *
     * @param username The Username. Default is 'admin'
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password of the thing.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the thing.
     *
     * @param password The password. Default is '1234'
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the URL to the thing.
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the thing.
     *
     * @param url The URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("IP", this.getIpAddress()).append("user", this.getUsername())
                .toString();
    }
}
