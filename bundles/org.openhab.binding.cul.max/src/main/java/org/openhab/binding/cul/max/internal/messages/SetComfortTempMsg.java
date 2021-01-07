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
 * Factory reset device
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class SetComfortTempMsg extends BaseMsg {
    private static final int SET_COMFORT_TEMP_MESSAGE_PAYLOAD_LEN = 0; /* in bytes */

    public SetComfortTempMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr) {
        super(msgCount, msgFlag, MaxCulMsgType.SET_COMFORT_TEMPERATURE, groupId, srcAddr, dstAddr);
        /* empty payload */
        byte[] payload = new byte[SET_COMFORT_TEMP_MESSAGE_PAYLOAD_LEN];
        super.appendPayload(payload);
    }
}
