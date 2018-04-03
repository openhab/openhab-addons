/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.config;

/**
 * The {@link PlugwiseStickConfig} class represents the configuration for a Plugwise Stick.
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseStickConfig {

    private String serialPort;
    private int messageWaitTime = 150; // milliseconds

    public String getSerialPort() {
        return serialPort;
    }

    public int getMessageWaitTime() {
        return messageWaitTime;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public void setMessageWaitTime(int messageWaitTime) {
        this.messageWaitTime = messageWaitTime;
    }

    @Override
    public String toString() {
        return "PlugwiseStickConfig [serialPort=" + serialPort + ", messageWaitTime=" + messageWaitTime + "]";
    }
}
