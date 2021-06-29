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
import org.openhab.binding.cul.max.internal.messages.constants.PushButtonMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message from Push Button devices
 *
 * @author Paul Hampson (cyclingengineer) - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.6.0
 */
@NonNullByDefault
public class PushButtonMsg extends BaseMsg implements BatteryStateMsg, RfErrorStateMsg {

    private static final int PUSH_BUTTON_PAYLOAD_LEN = 2; /* in bytes */

    private PushButtonMode mode = PushButtonMode.UNKNOWN;
    private boolean isRetransmission = false;
    private boolean batteryLow;

    private boolean rfError;

    private final Logger logger = LoggerFactory.getLogger(PushButtonMsg.class);

    public PushButtonMsg(String rawMsg) throws MaxCulProtocolException {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);

        if (this.payload.length == PUSH_BUTTON_PAYLOAD_LEN) {
            if (this.payload[0] == 0x50) {
                // behaviour
                isRetransmission = true;
            }

            if (this.payload[1] == 0x0) {
                mode = PushButtonMode.ECO;
            } else if (this.payload[1] == 0x1) {
                mode = PushButtonMode.AUTO;
            }
        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
        rfError = extractBitFromByte(this.payload[0], 6);
        /* extract battery status */
        batteryLow = extractBitFromByte(this.payload[0], 7);
    }

    public PushButtonMode getMode() {
        return mode;
    }

    public boolean isRetransmission() {
        return isRetransmission;
    }

    @Override
    public boolean isBatteryLow() {
        return batteryLow;
    }

    @Override
    public boolean isRfError() {
        return rfError;
    }

    @Override
    protected void printFormattedPayload() {
        super.printFormattedPayload();
        logger.debug("\tMode                => {}", mode);
        logger.debug("\tRF Error            => {}", rfError);
        logger.debug("\tBattery Low         => {}", batteryLow);
    }
}
