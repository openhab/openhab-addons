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
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does preprocessing of messages to decide which handler should be called.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class MessageDispatcher extends BaseFeatureHandler {
    protected final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    public MessageDispatcher(DeviceFeature feature) {
        super(feature);
    }

    /**
     * Handles an incoming broadcast message
     *
     * @param msg the message received
     * @param feature the device feature
     */
    protected void handleBroadcastMessage(Msg msg, DeviceFeature feature) throws FieldException {
        byte cmd1 = msg.isAllLinkSuccessReport() ? msg.getInsteonAddress("toAddress").getHighByte()
                : msg.getByte("command1");
        int group = msg.getGroup();
        MessageHandler handler = feature.getMsgHandler(cmd1, group);
        if (handler == null) {
            logger.trace("{}:{} ignoring msg as not for this feature", getDevice().getAddress(), feature.getName());
        } else if (handler.canHandle(msg)) {
            logger.debug("{}:{}->{} {} group:{}", getDevice().getAddress(), feature.getName(),
                    handler.getClass().getSimpleName(), msg.getType(), group != -1 ? group : "N/A");
            handler.handleMessage(cmd1, msg);
        }
    }

    /**
     * Handles an incoming direct message
     *
     * @param msg the message received
     * @param feature the device feature
     */
    protected void handleDirectMessage(Msg msg, DeviceFeature feature) throws FieldException {
        byte cmd1 = msg.getByte("command1");
        int group = msg.getGroup();
        // determine msg handler using cmd 0x19 on DIRECT ACK/NACK reply messages
        MessageHandler handler = feature.getOrDefaultMsgHandler(msg.isAckOrNackOfDirect() ? 0x19 : cmd1, group);
        if (handler.canHandle(msg)) {
            logger.debug("{}:{}->{} {} group:{}", getDevice().getAddress(), feature.getName(),
                    handler.getClass().getSimpleName(), msg.getType(), group != -1 ? group : "N/A");
            handler.handleMessage(cmd1, msg);
        }
    }

    /**
     * Handles an incoming im message
     *
     * @param msg the message received
     * @param feature the device feature
     */
    protected void handleIMMessage(Msg msg, DeviceFeature feature) throws FieldException {
        byte cmd = msg.getCommand();
        MessageHandler handler = feature.getOrDefaultMsgHandler(cmd);
        logger.debug("{}:{}->{} IM", getDevice().getAddress(), feature.getName(), handler.getClass().getSimpleName());
        handler.handleMessage(cmd, msg);
    }

    /**
     * Dispatches message
     *
     * @param msg Message to dispatch
     * @return true if this message was found to be a reply to a direct message,
     *         and was claimed by one of the handlers
     */
    public abstract boolean dispatch(Msg msg);

    public static class DefaultDispatcher extends MessageDispatcher {
        DefaultDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                if (msg.isAllLinkCleanupAckOrNack()) {
                    // Had cases when a KeypadLinc would send an ALL_LINK_CLEANUP_ACK
                    // in response to a direct status query message
                    return false;
                }
                if (msg.isAllLinkBroadcastOrCleanup() || msg.isBroadcast()) {
                    handleBroadcastMessage(msg, feature);
                    return false;
                }
                if (msg.isDirect() || feature.isMyDirectAck(msg)) {
                    // handle DIRECT and my ACK messages queried by this feature
                    handleDirectMessage(msg, feature);
                }
                return feature.isMyDirectAckOrNack(msg);
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    public static class DefaultGroupDispatcher extends MessageDispatcher {
        DefaultGroupDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                if (feature.isMyDirectAck(msg)) {
                    // handle my DIRECT ACK messages queried by this feature
                    handleDirectMessage(msg, feature);
                    // get connected features to handle my DIRECT ACK messages
                    for (DeviceFeature connectedFeature : feature.getConnectedFeatures()) {
                        handleDirectMessage(msg, connectedFeature);
                    }
                }
                return feature.isMyDirectAckOrNack(msg);
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    public static class PollGroupDispatcher extends MessageDispatcher {
        PollGroupDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            if (feature.isMyDirectAckOrNack(msg)) {
                logger.debug("{}:{} got poll {}", getDevice().getAddress(), feature.getName(), msg.getType());
                return true;
            }
            return false;
        }
    }

    public static class PassThroughDispatcher extends MessageDispatcher {
        PassThroughDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                byte cmd1 = msg.getByte("command1");
                int group = msg.getGroup();
                MessageHandler handler = feature.getDefaultMsgHandler();
                if (handler.canHandle(msg)) {
                    logger.debug("{}:{}->{} {} group:{}", getDevice().getAddress(), feature.getName(),
                            handler.getClass().getSimpleName(), msg.getType(), group != -1 ? group : "N/A");
                    handler.handleMessage(cmd1, msg);
                }
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    public static class IMDispatcher extends MessageDispatcher {
        IMDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                if (feature.isMyReply(msg)) {
                    if (msg.isReplyAck()) {
                        handleIMMessage(msg, feature);
                    }
                    return true;
                }
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    public static class IMGroupDispatcher extends MessageDispatcher {
        IMGroupDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                if (feature.isMyReply(msg)) {
                    if (msg.isReplyAck()) {
                        // get connected features to handle my reply ACK messages
                        for (DeviceFeature connectedFeature : feature.getConnectedFeatures()) {
                            handleIMMessage(msg, connectedFeature);
                        }
                    }
                    return true;
                }
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    public static class X10Dispatcher extends MessageDispatcher {
        X10Dispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                byte cmd = msg.getByte("rawX10");
                MessageHandler handler = feature.getOrDefaultMsgHandler(cmd);
                logger.debug("{}:{}->{} X10", getX10Device().getAddress(), feature.getName(),
                        handler.getClass().getSimpleName());
                handler.handleMessage(cmd, msg);
            } catch (FieldException e) {
                logger.warn("error parsing, dropping msg {}", msg);
            }
            return false;
        }
    }

    /**
     * Drop all incoming messages silently
     */
    public static class NoOpDispatcher extends MessageDispatcher {
        NoOpDispatcher(DeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            return false;
        }
    }

    /**
     * Factory method for creating a dispatcher of a given name using java reflection
     *
     * @param name the name of the dispatcher to create
     * @param parameters the parameters of the handler to create
     * @param feature the feature for which to create the dispatcher
     * @return the handler which was created
     */
    public static @Nullable <T extends MessageDispatcher> T makeHandler(String name, Map<String, String> parameters,
            DeviceFeature feature) {
        try {
            String className = MessageDispatcher.class.getName() + "$" + name;
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
