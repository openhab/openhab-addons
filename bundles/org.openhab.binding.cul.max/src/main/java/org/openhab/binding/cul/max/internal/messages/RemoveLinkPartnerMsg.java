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

import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulMsgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message to remove associate devices with each other
 *
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 */
public class RemoveLinkPartnerMsg extends BaseMsg {
    final private int REMOVE_LINK_PARTNER_PAYLOAD_LEN = 4;

    private static final Logger logger = LoggerFactory.getLogger(RemoveLinkPartnerMsg.class);

    private MaxCulDevice devType;
    private String partnerAddr;

    public RemoveLinkPartnerMsg(byte msgCount, byte msgFlag, byte groupId, String srcAddr, String dstAddr,
            String partnerAddr, MaxCulDevice devType) {
        super(msgCount, msgFlag, MaxCulMsgType.REMOVE_LINK_PARTNER, groupId, srcAddr, dstAddr);

        this.partnerAddr = partnerAddr;
        this.devType = devType;
        byte[] payload = new byte[REMOVE_LINK_PARTNER_PAYLOAD_LEN];

        payload[0] = (byte) (Integer.parseInt(partnerAddr.substring(0, 2), 16) & 0xff);
        payload[1] = (byte) (Integer.parseInt(partnerAddr.substring(2, 4), 16) & 0xff);
        payload[2] = (byte) (Integer.parseInt(partnerAddr.substring(4, 6), 16) & 0xff);
        payload[3] = (byte) devType.getDeviceTypeInt();
        super.appendPayload(payload);
    }

    @Override
    protected void printFormattedPayload() {
        super.printFormattedPayload();
        logger.debug("\tDevice Type  => {}", this.devType);
        logger.debug("\tPartner Addr => {}", this.partnerAddr);
    }
}
