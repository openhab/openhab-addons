/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    @Nullable
    private String port;
    private int id;
    private int baud;
    @Nullable
    private String stopBits;
    @Nullable
    private String parity;
    private int dataBits;
    @Nullable
    private String encoding;
    private boolean echo;
    private int receiveTimeoutMillis;
    @Nullable
    private String flowControlIn;
    @Nullable
    private String flowControlOut;
    private int timeBetweenTransactionsMillis;
    private int connectMaxTries;
    private int connectTimeoutMillis;

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

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

}
