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
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data Structure for class SendDispatcherThread
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class PacketStruct {
    private DatagramPacket packet;

    public DatagramPacket getPacket() {
        return packet;
    }

    private boolean sent = false;
    private long time = 0;

    public PacketStruct(DatagramPacket packetPar) {
        packet = packetPar;
    }

    public long getTime() {
        return time;
    }

    public boolean getSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public void setTime(long time) {
        // set the time only if it has not already been set once
        if (this.time == 0) {
            this.time = time;
        }
    }
}
