/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PollHandler creates an Insteon message to query a particular
 * DeviceFeature of an Insteon device.
 *
 * @author Bernd Pfrommer
 * @since 1.5.0
 */
public abstract class PollHandler {
    private static final Logger logger = LoggerFactory.getLogger(PollHandler.class);
    DeviceFeature m_feature = null;
    ExtendedData extended = ExtendedData.extendedNone;
    StandardInsteonMessages cmd1;
    byte cmd2;
    byte data1;
    byte data2;
    byte data3;

    /**
     * Constructor
     *
     * @param feature The device feature being polled
     */
    PollHandler(DeviceFeature feature) {
        m_feature = feature;
    }

    /**
     * Creates Insteon message that can be used to poll a feature
     * via the Insteon network.
     *
     * @param device reference to the insteon device to be polled
     * @return Insteon query message or null if creation failed
     */
    public abstract SendInsteonMessage makeMsg(InsteonThingHandler device);

    public void setExtended(ExtendedData data) {
        extended = data;
    }

    public void setCmd1(StandardInsteonMessages value) {
        cmd1 = value;
    }

    public void setCmd2(byte value) {
        cmd2 = value;
    }

    public void setData1(byte value) {
        data1 = value;
    }

    public void setData2(byte value) {
        data2 = value;
    }

    public void setData3(byte value) {
        data3 = value;
    }

    /**
     * A flexible, parameterized poll handler that can generate
     * most query messages. Provide the suitable parameters in
     * the device features file.
     */

    public static class FlexPollHandler extends PollHandler {

        FlexPollHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public SendInsteonMessage makeMsg(InsteonThingHandler d) {
            SendInsteonMessage m = null;
            if (extended != ExtendedData.extendedNone) {
                m = new SendInsteonMessage(d.getAddress(), new InsteonFlags(), cmd1, cmd2,
                        new byte[] { data1, data2, data3 });
                /**
                 * if (extended == ExtendedData.extendedCrc1) {
                 * m.setCRC();
                 * } else if (extended == ExtendedData.extendedCrc2) {
                 * m.setCRC2();
                 * }
                 */
            } else {
                m = new SendInsteonMessage(d.getAddress(), new InsteonFlags(), cmd1, cmd2);
            }
            m.setQuietTime(500L);
            return m;
        }
    }

    public static class NoPollHandler extends PollHandler {
        NoPollHandler(DeviceFeature f) {
            super(f);
        }

        @Override
        public SendInsteonMessage makeMsg(InsteonThingHandler d) {
            return null;
        }
    }
}
