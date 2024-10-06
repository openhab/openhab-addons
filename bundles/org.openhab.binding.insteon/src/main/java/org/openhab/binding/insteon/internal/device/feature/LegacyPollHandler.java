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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.LegacyDevice;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.ParameterParser;
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
public abstract class LegacyPollHandler {
    protected final Logger logger = LoggerFactory.getLogger(LegacyPollHandler.class);

    LegacyDeviceFeature feature;
    Map<String, String> parameters = new HashMap<>();

    /**
     * Constructor
     *
     * @param feature The device feature being polled
     */
    LegacyPollHandler(LegacyDeviceFeature feature) {
        this.feature = feature;
    }

    /**
     * Creates Insteon message that can be used to poll a feature
     * via the Insteon network.
     *
     * @param device reference to the insteon device to be polled
     * @return Insteon query message or null if creation failed
     */
    public abstract @Nullable Msg makeMsg(LegacyDevice device);

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns parameter as integer
     *
     * @param key key of parameter
     * @param def default
     * @return value of parameter
     */
    protected int getIntParameter(String key, int def) {
        return ParameterParser.getParameterAsOrDefault(parameters.get(key), Integer.class, def);
    }

    /**
     * A flexible, parameterized poll handler that can generate
     * most query messages. Provide the suitable parameters in
     * the device features file.
     */
    public static class FlexPollHandler extends LegacyPollHandler {
        FlexPollHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public @Nullable Msg makeMsg(LegacyDevice device) {
            Msg msg = null;
            int cmd1 = getIntParameter("cmd1", 0);
            int cmd2 = getIntParameter("cmd2", 0);
            int ext = getIntParameter("ext", -1);
            try {
                if (ext == 1 || ext == 2) {
                    int d1 = getIntParameter("d1", 0);
                    int d2 = getIntParameter("d2", 0);
                    int d3 = getIntParameter("d3", 0);
                    msg = Msg.makeExtendedMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) cmd2,
                            new byte[] { (byte) d1, (byte) d2, (byte) d3 }, false);
                    if (ext == 1) {
                        msg.setCRC();
                    } else if (ext == 2) {
                        msg.setCRC2();
                    }
                } else {
                    msg = Msg.makeStandardMessage((InsteonAddress) device.getAddress(), (byte) cmd1, (byte) cmd2);
                }
                msg.setQuietTime(500L);
            } catch (FieldException e) {
                logger.warn("error setting field in msg: ", e);
            } catch (InvalidMessageTypeException e) {
                logger.warn("invalid message ", e);
            }
            return msg;
        }
    }

    public static class NoPollHandler extends LegacyPollHandler {
        NoPollHandler(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public @Nullable Msg makeMsg(LegacyDevice device) {
            return null;
        }
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param params
     * @param feature the feature for which to create the handler
     * @return the handler which was created
     */
    @Nullable
    public static <T extends LegacyPollHandler> T makeHandler(String name, Map<String, String> params,
            LegacyDeviceFeature feature) {
        try {
            String className = LegacyPollHandler.class.getName() + "$" + name;
            @SuppressWarnings("unchecked")
            Class<? extends T> classRef = (Class<? extends T>) Class.forName(className);
            @Nullable
            T handler = classRef.getDeclaredConstructor(LegacyDeviceFeature.class).newInstance(feature);
            handler.setParameters(params);
            return handler;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
    }
}
