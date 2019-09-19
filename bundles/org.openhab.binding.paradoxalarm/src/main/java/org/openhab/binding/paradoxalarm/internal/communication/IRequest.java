/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * The {@link IRequest} - interface definition for the request used in the communication.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface IRequest {

    IPPacketPayload getRequestPayload();

    void setTimeStamp();

    boolean isTimeStampExpired(long expirationTreshold);

    RequestType getType();
}
