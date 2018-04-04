/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link: N_Message} contains information about a newly discovered Device
 * This is the response to a n: command
 *
 * @author Marcel Verpaalen - Initial Version
 * @since 2.0.0
 */
public final class N_Message extends Message {
    private final Logger logger = LoggerFactory.getLogger(N_Message.class);

    private String decodedPayload;
    private DeviceType deviceType = null;
    private String rfAddress = null;
    private String serialnr = null;

    /**
     * The {@link: N_Message} contains information about a newly discovered Device
     *
     * @param raw String with raw message
     */
    public N_Message(String raw) {
        super(raw);
        String msgPayload = this.getPayload();

        if (msgPayload.length() > 0) {
            try {
                decodedPayload = new String(Base64.decodeBase64(msgPayload), "UTF-8");
                byte[] bytes = Base64.decodeBase64(msgPayload);

                deviceType = DeviceType.create(bytes[0] & 0xFF);
                rfAddress = Utils.toHex(bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF);

                byte[] data = new byte[10];
                System.arraycopy(bytes, 4, data, 0, 10);
                serialnr = new String(data, "UTF-8");
            } catch (Exception e) {
                logger.debug("Exception occurred during parsing of N message: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("No device found during inclusion");
        }
    }

    /**
     * @return the deviceType
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * @return the rf Address
     */
    public String getRfAddress() {
        return rfAddress;
    }

    /**
     * @return the Serial Number
     */
    public String getSerialNumber() {
        return serialnr;
    }

    @Override
    public void debug(Logger logger) {
        if (this.rfAddress != null) {
            logger.debug("=== N_Message === ");
            logger.trace("\tRAW : {}", this.decodedPayload);
            logger.debug("\tDevice Type    : {}", this.deviceType.toString());
            logger.debug("\tRF Address     : {}", this.rfAddress);
            logger.debug("\tSerial         : {}", this.serialnr);
        } else {
            logger.trace("=== N_Message === ");
            logger.trace("\tRAW : {}", this.decodedPayload);
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.N;
    }
}
