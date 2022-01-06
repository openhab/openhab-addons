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
package org.openhab.binding.paradoxalarm.internal.communication;

import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacket;

/**
 * The {@link Request}. Abstract request class. Used to be derived for the particular types of requests to Paradox.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class Request implements IRequest {

    private IPPacket packet;
    private long timestamp;
    private RequestType type;
    private IResponseReceiver receiver;

    public Request(RequestType type, IPPacket packet, IResponseReceiver receiver) {
        this.packet = packet;
        this.type = type;
        this.receiver = receiver;
    }

    @Override
    public IPPacket getRequestPacket() {
        return packet;
    }

    @Override
    public void setTimeStamp() {
        timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean isTimeStampExpired(long tresholdInMillis) {
        return System.currentTimeMillis() - timestamp >= tresholdInMillis;
    }

    @Override
    public RequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Request [packet=" + packet + ", timestamp=" + timestamp + ", type=" + type + "]";
    }

    @Override
    public IResponseReceiver getResponseReceiver() {
        return receiver;
    }
}
