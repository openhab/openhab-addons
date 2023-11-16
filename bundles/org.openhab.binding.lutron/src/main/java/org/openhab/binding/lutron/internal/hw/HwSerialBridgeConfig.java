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
package org.openhab.binding.lutron.internal.hw;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.internal.hw.HwSerialBridgeHandler}.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwSerialBridgeConfig {
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baudRate";
    public static final String UPDATE_TIME = "updateTime";
    public static final Integer DEFAULT_BAUD = 9600;

    private String serialPort;
    private Integer baudRate = DEFAULT_BAUD;
    private Boolean updateTime;

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public Integer getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(Integer baudRate) {
        this.baudRate = baudRate;
    }

    public Boolean getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Boolean updateTime) {
        this.updateTime = updateTime;
    }
}
