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
package org.openhab.binding.enocean.internal.eep.Base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTeachInConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPType;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class _4BSMessage extends EEP {

    protected boolean supportsTeachInVariation3 = false;

    public boolean isTeachInVariation3Supported() {
        return supportsTeachInVariation3;
    }

    public _4BSMessage(ERP1Message packet) {
        super(packet);
    }

    public _4BSMessage() {
        super();
    }

    public static final byte TEACHIN_BIT = 0x08;
    public static final byte LRN_TYPE_MASK = (byte) 0x80;

    public long getDBByOffsetSizeValue(int offset, int size) {
        if ((offset < 0 || offset > 31) || (size < 1 || size > 32 - offset)) {
            logger.warn("4BSMessage get DB value by offset: {} and size: {}", offset, size);
            return 0;
        }

        long msg = (((long) bytes[0] & 0xFF) << 24) | (((long) bytes[1] & 0xFF) << 16) | (((long) bytes[2] & 0xFF) << 8)
                | bytes[3];
        msg = (msg >> (32 - offset - size)) & ((1 << size) - 1);

        logger.debug("_4BSMessage get DB value message bytes {} {} {} {} resulted in {} with offset: {} and size: {}",
                bytes[0], bytes[1], bytes[2], bytes[3], msg, offset, size);

        return msg;
    }

    public byte getDB0() {
        return bytes[3];
    }

    public int getDB0Value() {
        return (getDB0() & 0xFF);
    }

    public byte getDB1() {
        return bytes[2];
    }

    public int getDB1Value() {
        return (getDB1() & 0xFF);
    }

    public byte getDB2() {
        return bytes[1];
    }

    public int getDB2Value() {
        return (getDB2() & 0xFF);
    }

    public byte getDB3() {
        return bytes[0];
    }

    public int getDB3Value() {
        return (getDB3() & 0xFF);
    }

    @Override
    protected void teachInQueryImpl(@Nullable Configuration config) {
        if (config == null) {
            return;
        }

        EnOceanChannelTeachInConfig c = config.as(EnOceanChannelTeachInConfig.class);
        if (c.teachInMSG.isEmpty()) {
            EEPType type = getEEPType();

            byte db3 = (byte) ((getEEPType().getFunc() << 2) | ((type.getType()) >>> 5));
            byte db2 = (byte) ((type.getType() << 3) & 0xff);
            byte db1 = 0;

            try {
                int manufId = (Integer.parseInt(c.manufacturerId, 16) & 0x7ff); // => 11 bit
                db2 += (manufId >>> 8);
                db1 += (manufId & 0xff);
            } catch (Exception e) {
            }

            setData(db3, db2, db1, LRN_TYPE_MASK);
        } else {
            try {
                byte[] msg = HexUtils.hexToBytes(c.teachInMSG);
                setData(msg);
            } catch (IllegalArgumentException e) {
                logger.debug("Command TeachIn could not transformed");
                throw e;
            }
        }
    }
}
