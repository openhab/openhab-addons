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
 * Wakeup message
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class WakeupMsg extends BaseMsg {

    private static final int WAKEUP_MSG_PAYLOAD_LEN = 1;

    public WakeupMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr) {
        super(msgCount, msgFlag, MaxCulMsgType.WAKEUP, groupId, srcAddr, dstAddr);

        byte[] payload = new byte[WAKEUP_MSG_PAYLOAD_LEN];

        payload[0] = 0x3f; // no idea what this means, just always 0x3f?
                           // suspected to be number of seconds device is awake
                           // for?

        super.appendPayload(payload);
    }

    public WakeupMsg(String rawmsg) throws MaxCulProtocolException {
        super(rawmsg);
    }
}
