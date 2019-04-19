/**
 * Copyright (c) 2010-2018 by the respective copyright holders.

 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.omnilink.config;

/**
 *
 * @author Craig Hamilton
 *
 */
public class OmnilinkBridgeConfig {

    private String key1;
    private String key2;
    private String ipAddress;
    private int port;
    private int logPollingSeconds;

    public int getLogPollingSeconds() {
        return logPollingSeconds;
    }

    public void setLogPollingSeconds(int logPollingSeconds) {
        this.logPollingSeconds = logPollingSeconds;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
