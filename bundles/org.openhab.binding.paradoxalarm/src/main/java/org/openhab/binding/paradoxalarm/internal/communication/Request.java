/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacketPayload;

/**
 * The {@link Request}. Abstract request class. Used to be derived for the particular types of requests to Paradox.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class Request implements IRequest {

    private IPPacketPayload payload;
    private long timestamp;
    private RequestType type;

    public Request(RequestType type, IPPacketPayload payload) {
        this.payload = payload;
        this.type = type;
    }

    @Override
    public IPPacketPayload getRequestPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Request [type=" + type + "]";
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
}
