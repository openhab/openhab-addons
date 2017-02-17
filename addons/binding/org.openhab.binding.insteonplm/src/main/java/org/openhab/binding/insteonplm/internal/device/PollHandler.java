/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.io.IOException;

import org.openhab.binding.insteonplm.InsteonPLMBindingConstants.ExtendedData;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.MessageFactory;
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
    byte cmd1;
    byte cmd2;
    byte data1;
    byte data2;
    byte data3;

    /**
     * Constructor
     *
     * @param feature The device feature being polled
     */
    PollHandler(DeviceFeature feature, MessageFactory messageFactory) {
        m_feature = feature;
    }

    /**
     * Creates Insteon message that can be used to poll a feature
     * via the Insteon network.
     *
     * @param device reference to the insteon device to be polled
     * @return Insteon query message or null if creation failed
     */
    public abstract Message makeMsg(InsteonThingHandler device);

    public void setExtended(ExtendedData data) {
        extended = data;
    }

    public void setCmd1(byte value) {
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

        FlexPollHandler(DeviceFeature f, MessageFactory factory) {
            super(f, factory);
        }

        @Override
        public Message makeMsg(InsteonThingHandler d) {
            Message m = null;
            try {
                if (extended != ExtendedData.extendedNone) {
                    m = d.getMessageFactory().makeExtendedMessage((byte) 0x0f, cmd1, cmd2,
                            new byte[] { data1, data2, data3 }, d.getAddress());
                    if (extended == ExtendedData.extendedCrc1) {
                        m.setCRC();
                    } else if (extended == ExtendedData.extendedCrc2) {
                        m.setCRC2();
                    }
                } else {
                    m = d.getMessageFactory().makeStandardMessage((byte) 0x0f, cmd1, cmd2, d.getAddress());
                }
                m.setQuietTime(500L);
            } catch (FieldException e) {
                logger.warn("error setting field in msg: ", e);
            } catch (IOException e) {
                logger.error("poll failed with exception ", e);
            }
            return m;
        }
    }

    public static class NoPollHandler extends PollHandler {
        NoPollHandler(DeviceFeature f, MessageFactory messageFactory) {
            super(f, messageFactory);
        }

        @Override
        public Message makeMsg(InsteonThingHandler d) {
            return null;
        }
    }
}
