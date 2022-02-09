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

package org.openhab.binding.smsmodem.internal.smslib.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Extracted from SMSLib
 *
 * @author Gwendal ROULLEAU - Initial contribution, extracted from SMSLib
 */
@NonNullByDefault
public class OutboundBinaryMessage extends OutboundMessage {
    private static final long serialVersionUID = 1L;

    public OutboundBinaryMessage() {
    }

    public OutboundBinaryMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, byte[] data) {
        super(originatorAddress, recipientAddress, new Payload(data));
        setEncoding(Encoding.Enc8);
    }
}
