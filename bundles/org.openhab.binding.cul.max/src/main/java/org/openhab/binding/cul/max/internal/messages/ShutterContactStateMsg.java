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
import org.openhab.binding.cul.max.internal.messages.constants.ShutterContactState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message class to handle shutter contact state messages
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 * @since 1.8.0
 */
@NonNullByDefault
public class ShutterContactStateMsg extends BaseMsg implements BatteryStateMsg, RfErrorStateMsg {

    private static final int SHUTTER_CONTACT_STATE_PAYLOAD_LEN = 1; /* in bytes */

    private boolean batteryLow;

    private boolean rfError;

    private ShutterContactState state = ShutterContactState.UNKNOWN;

    private final Logger logger = LoggerFactory.getLogger(ShutterContactStateMsg.class);

    public ShutterContactStateMsg(String rawMsg) throws MaxCulProtocolException {
        super(rawMsg);
        logger.debug("{} Payload Len -> {}", this.msgType, this.payload.length);

        if (this.payload.length == SHUTTER_CONTACT_STATE_PAYLOAD_LEN) {
            boolean isOpen = extractBitFromByte(this.payload[0], 1);
            ;
            if (!isOpen) {
                state = ShutterContactState.CLOSED;
            } else {
                state = ShutterContactState.OPEN;
            }
            rfError = extractBitFromByte(this.payload[0], 6);
            /* extract battery status */
            batteryLow = extractBitFromByte(this.payload[0], 7);
        } else {
            logger.error("Got {} message with incorrect length!", this.msgType);
        }
    }

    public ShutterContactState getState() {
        return state;
    }

    @Override
    protected void printFormattedPayload() {
        super.printFormattedPayload();
        logger.debug("\tState               => {}", state);
        logger.debug("\tRF Error            => {}", rfError);
        logger.debug("\tBattery Low         => {}", batteryLow);
    }

    @Override
    public boolean isBatteryLow() {
        return batteryLow;
    }

    @Override
    public boolean isRfError() {
        return rfError;
    }
}
