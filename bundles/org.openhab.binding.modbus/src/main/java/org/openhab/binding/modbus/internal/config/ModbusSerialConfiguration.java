/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Configuration for serial thing
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusSerialConfiguration {
    private @Nullable String port;
    private int id = 1;
    private int baud;
    private @Nullable String stopBits;
    private @Nullable String parity;
    private int dataBits;
    private String encoding = "rtu";
    private boolean echo;
    private int receiveTimeoutMillis = 1500;
    private String flowControlIn = "none";
    private String flowControlOut = "none";
    private int timeBetweenTransactionsMillis = 35;
    private int connectMaxTries = 1;
    private int afterConnectionDelayMillis;
    private int connectTimeoutMillis = 10_000;
    private boolean enableDiscovery;

    public @Nullable String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public @Nullable String getStopBits() {
        return stopBits;
    }

    public void setStopBits(String stopBits) {
        this.stopBits = stopBits;
    }

    public @Nullable String getParity() {
        return parity;
    }

    public void setParity(String parity) {
        this.parity = parity;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public @Nullable String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isEcho() {
        return echo;
    }

    public void setEcho(boolean echo) {
        this.echo = echo;
    }

    public int getReceiveTimeoutMillis() {
        return receiveTimeoutMillis;
    }

    public void setReceiveTimeoutMillis(int receiveTimeoutMillis) {
        this.receiveTimeoutMillis = receiveTimeoutMillis;
    }

    public @Nullable String getFlowControlIn() {
        return flowControlIn;
    }

    public void setFlowControlIn(String flowControlIn) {
        this.flowControlIn = flowControlIn;
    }

    public @Nullable String getFlowControlOut() {
        return flowControlOut;
    }

    public void setFlowControlOut(String flowControlOut) {
        this.flowControlOut = flowControlOut;
    }

    public int getTimeBetweenTransactionsMillis() {
        return timeBetweenTransactionsMillis;
    }

    public void setTimeBetweenTransactionsMillis(int timeBetweenTransactionsMillis) {
        this.timeBetweenTransactionsMillis = timeBetweenTransactionsMillis;
    }

    public int getConnectMaxTries() {
        return connectMaxTries;
    }

    public void setConnectMaxTries(int connectMaxTries) {
        this.connectMaxTries = connectMaxTries;
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
