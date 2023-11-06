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
package org.openhab.binding.plugwise.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlugwiseStickConfig} class represents the configuration for a Plugwise Stick.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseStickConfig {

    private String serialPort = "";
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
