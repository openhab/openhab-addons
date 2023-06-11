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
package org.openhab.binding.modbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration for tcp thing
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusTcpConfiguration {
    private @Nullable String host;
    private int port;
    private int id = 1;
    private int timeBetweenTransactionsMillis = 60;
    private int timeBetweenReconnectMillis;
    private int connectMaxTries = 1;
    private int reconnectAfterMillis;
    private int afterConnectionDelayMillis;
    private int connectTimeoutMillis = 10_000;
    private boolean enableDiscovery;
    private boolean rtuEncoded;

    public boolean getRtuEncoded() {
        return rtuEncoded;
    }

    public @Nullable String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeBetweenTransactionsMillis() {
        return timeBetweenTransactionsMillis;
    }

    public void setTimeBetweenTransactionsMillis(int timeBetweenTransactionsMillis) {
        this.timeBetweenTransactionsMillis = timeBetweenTransactionsMillis;
    }

    public int getTimeBetweenReconnectMillis() {
        return timeBetweenReconnectMillis;
    }

    public void setTimeBetweenReconnectMillis(int timeBetweenReconnectMillis) {
        this.timeBetweenReconnectMillis = timeBetweenReconnectMillis;
    }

    public int getConnectMaxTries() {
        return connectMaxTries;
    }

    public void setConnectMaxTries(int connectMaxTries) {
        this.connectMaxTries = connectMaxTries;
    }

    public int getReconnectAfterMillis() {
        return reconnectAfterMillis;
    }

    public void setReconnectAfterMillis(int reconnectAfterMillis) {
        this.reconnectAfterMillis = reconnectAfterMillis;
    }

    public int getAfterConnectionDelayMillis() {
        return afterConnectionDelayMillis;
    }

    public void setAfterConnectionDelayMillis(int afterConnectionDelayMillis) {
        this.afterConnectionDelayMillis = afterConnectionDelayMillis;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public boolean isDiscoveryEnabled() {
        return enableDiscovery;
    }

    public void setDiscoveryEnabled(boolean enableDiscovery) {
        this.enableDiscovery = enableDiscovery;
    }
}
