/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.messages;

import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;

/**
 * Ajust DST
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 * @since 1.6.0
 */
public class SetDstAdjustMsg extends BaseMsg {
    final static private int SET_DST_AJUST_MESSAGE_PAYLOAD_LEN = 1; /* in bytes */

    public SetDstAdjustMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr,
            boolean dstAjust) {
        super(msgCount, msgFlag, MaxCulMsgType.SET_ECO_TEMPERATURE, groupId, srcAddr, dstAddr);
        /* empty payload */
        byte[] payload = new byte[SET_DST_AJUST_MESSAGE_PAYLOAD_LEN];
        payload[0] = dstAjust ? (byte) 0x1 : (byte) 0x0;
        super.appendPayload(payload);
    }
}
