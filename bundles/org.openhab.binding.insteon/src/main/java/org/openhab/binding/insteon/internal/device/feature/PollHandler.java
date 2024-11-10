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
package org.openhab.binding.insteon.internal.device.feature;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PollHandler creates an Insteon message to query a particular
 * DeviceFeature of an Insteon device.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class PollHandler extends BaseFeatureHandler {
    protected final Logger logger = LoggerFactory.getLogger(PollHandler.class);

    public PollHandler(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Creates Insteon message that can be used to poll a device feature
     *
     * @return Insteon query message or null if creation failed
     */
    public abstract @Nullable Msg makeMsg();

    public static class FlexPollHandler extends PollHandler {
        FlexPollHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public @Nullable Msg makeMsg() {
            Msg msg = null;
            InsteonAddress address = getInsteonDevice().getAddress();
            int cmd1 = getParameterAsInteger("cmd1", 0);
            int cmd2 = getParameterAsInteger("cmd2", 0);
            int ext = getParameterAsInteger("ext", -1);
            long quietTime = getParameterAsLong("quiet", -1);
            try {
                // make message based on feature parameters
                if (ext == 0) {
                    msg = Msg.makeStandardMessage(address, (byte) cmd1, (byte) cmd2);
                } else if (ext == 1 || ext == 2) {
                    // set userData1 to d1 parameter if defined, fallback to group parameter
                    byte[] data = { (byte) getParameterAsInteger("d1", getParameterAsInteger("group", 0)),
                            (byte) getParameterAsInteger("d2", 0), (byte) getParameterAsInteger("d3", 0) };
                    boolean setCRC = getInsteonDevice().getInsteonEngine().supportsChecksum();
                    if (ext == 1) {
                        msg = Msg.makeExtendedMessage(address, (byte) cmd1, (byte) cmd2, data, setCRC);
                    } else {
                        msg = Msg.makeExtendedMessageCRC2(address, (byte) cmd1, (byte) cmd2, data);
                    }
                } else {
                    logger.warn("{}: handler misconfigured, no valid ext field specified", nm());
                }
                // override default message quiet time if parameter specified
                if (msg != null && quietTime >= 0) {
                    msg.setQuietTime(quietTime);
                }
            } catch (FieldException e) {
                logger.warn("error setting field in msg: ", e);
            } catch (InvalidMessageTypeException e) {
                logger.warn("invalid message ", e);
            }
            return msg;
        }
    }

    public static class IMPollHandler extends PollHandler {
        IMPollHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public @Nullable Msg makeMsg() {
            Msg msg = null;
            int cmd = getParameterAsInteger("cmd", 0);
            try {
                msg = Msg.makeMessage((byte) cmd);
                byte[] data = msg.getData();
                int headerLength = msg.getHeaderLength();
                for (int i = headerLength; i < data.length; i++) {
                    data[i] = (byte) getParameterAsInteger("d" + (i - headerLength + 1), 0);
                }
            } catch (InvalidMessageTypeException e) {
                logger.warn("invalid message ", e);
            }
            return msg;
        }
    }

    public static class NoPollHandler extends PollHandler {
        NoPollHandler(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public @Nullable Msg makeMsg() {
            return null;
        }
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param parameters the parameters of the handler to create
     * @param feature the feature for which to create the handler
     * @return the handler which was created
     */
    public static @Nullable <T extends PollHandler> T makeHandler(String name, Map<String, String> parameters,
            DeviceFeature feature) {
        try {
            String className = PollHandler.class.getName() + "$" + name;
            @SuppressWarnings("unchecked")
            Class<? extends T> classRef = (Class<? extends T>) Class.forName(className);
            @Nullable
            T handler = classRef.getDeclaredConstructor(DeviceFeature.class).newInstance(feature);
            handler.setParameters(parameters);
            return handler;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
    }
}
