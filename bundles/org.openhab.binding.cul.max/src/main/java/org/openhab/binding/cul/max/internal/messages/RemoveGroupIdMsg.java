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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;

/**
 * RemoveGroupId Message
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 */
@NonNullByDefault
public class RemoveGroupIdMsg extends BaseMsg {

    private static final int REMOVE_GROUP_ID_PAYLOAD_LEN = 1;

    public RemoveGroupIdMsg(byte msgCount, byte msgFlag, String srcAddr, String dstAddr) {
        super(msgCount, msgFlag, MaxCulMsgType.REMOVE_GROUP_ID, (byte) 0x0, srcAddr, dstAddr);
        byte[] payload = new byte[REMOVE_GROUP_ID_PAYLOAD_LEN];
        payload[0] = (byte) 0x0;
        super.appendPayload(payload);
    }

    public RemoveGroupIdMsg(String rawmsg) throws MaxCulProtocolException {
        super(rawmsg);
    }
}
