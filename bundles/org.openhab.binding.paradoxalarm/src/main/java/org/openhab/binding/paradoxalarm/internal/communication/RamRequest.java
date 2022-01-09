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
 * The {@link RamRequest}. Request for retrieving RAM pages from Paradox system.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class RamRequest extends Request {

    private int ramBlockNumber;

    public RamRequest(int ramBlockNumber, IPPacket payload, IResponseReceiver receiver) {
        super(RequestType.RAM, payload, receiver);
        this.ramBlockNumber = ramBlockNumber;
    }

    public int getRamBlockNumber() {
        return ramBlockNumber;
    }

    @Override
    public String toString() {
        return "RamRequest [getType()=" + getType() + ", getRamBlockNumber()=" + getRamBlockNumber() + "]";
    }
}
