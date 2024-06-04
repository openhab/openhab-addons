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
package org.openhab.binding.upb.internal.handler;

import java.util.concurrent.CompletionStage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.upb.internal.message.MessageBuilder;

/**
 * Handler for PIM communications.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public interface UPBIoHandler {
    enum CmdStatus {
        ACK,
        NAK,
        WRITE_FAILED
    }

    CompletionStage<CmdStatus> sendPacket(MessageBuilder message);
}
