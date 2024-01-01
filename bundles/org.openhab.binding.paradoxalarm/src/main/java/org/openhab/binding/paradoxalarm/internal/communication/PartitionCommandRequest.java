/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link PartitionCommandRequest} Request object for wrapping partition command.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class PartitionCommandRequest extends Request {

    public PartitionCommandRequest(RequestType type, IPPacket packet, IResponseReceiver receiver) {
        super(type, packet, receiver);
    }
}
