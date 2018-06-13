/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

import org.openhab.binding.ihc2.internal.ws.Ihc2Client.DiscoveryLevel;

/**
 * The {@link Ihc2ControllerConfig} holds information on howto connect to the LK IHC Controller.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2ControllerConfig {

    private String ipAddress;
    private String user;
    private String password;
    private String projectFile;
    private String resourceFile;
    private int timeout;
    private String discoveryLevel;

    // public boolean sameConnectionParameters(Ihc2ControllerConfig config) {
    // return StringUtils.equals(this.ipAddress, config.ipAddress) && StringUtils.equals(this.user, config.user)
    // && StringUtils.equals(this.password, config.password)
    // && StringUtils.equals(this.projectFile, config.projectFile) && this.timeout == config.timeout;
    // }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(String projectFile) {
        this.projectFile = projectFile;
    }

    public String getResourceFile() {
        return resourceFile;
    }

    public void setResourceFile(String resourceFile) {
        this.resourceFile = resourceFile;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public DiscoveryLevel getDiscoveryLevel() {
        return DiscoveryLevel.valueOf(discoveryLevel);
    }

    public void setDiscoveryLevel(String discoveryLevel) {
        this.discoveryLevel = discoveryLevel;
    }

}
