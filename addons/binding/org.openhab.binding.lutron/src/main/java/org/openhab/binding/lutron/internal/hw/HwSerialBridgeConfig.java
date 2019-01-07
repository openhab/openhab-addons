/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.hw;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.handler.HWSerialBridgeHandler}.
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
