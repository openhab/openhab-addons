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
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;
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
public abstract class LegacyMessageDispatcher {
    protected final Logger logger = LoggerFactory.getLogger(LegacyMessageDispatcher.class);

    LegacyDeviceFeature feature;
    Map<String, String> parameters = new HashMap<>();

    /**
     * Constructor
     *
     * @param feature DeviceFeature to which this MessageDispatcher belongs
     */
    LegacyMessageDispatcher(LegacyDeviceFeature feature) {
        this.feature = feature;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Generic handling of incoming ALL LINK messages
     *
     * @param msg the message received
     * @return true if the message was handled by this function
     */
    protected boolean handleAllLinkMessage(Msg msg) {
        if (!msg.isAllLinkBroadcastOrCleanup()) {
            return false;
        }
        try {
            InsteonAddress address = msg.getInsteonAddress("toAddress");
            // ALL_LINK_BROADCAST and ALL_LINK_CLEANUP
            // have a valid Command1 field
            // but the CLEANUP_SUCCESS (of type ALL_LINK_BROADCAST!)
            // message has cmd1 = 0x06 and the cmd as the
            // high byte of the toAddress.
            byte cmd1 = msg.getByte("command1");
            if (!msg.isAllLinkCleanup() && cmd1 == 0x06) {
                cmd1 = address.getHighByte();
            }
            // For ALL_LINK_BROADCAST messages, the group is
            // in the low byte of the toAddress. For direct
            // ALL_LINK_CLEANUP, it is in Command2

            int group = (msg.isAllLinkCleanup() ? msg.getByte("command2") : address.getLowByte()) & 0xff;
            LegacyMessageHandler handler = feature.getMsgHandlers().get(cmd1 & 0xFF);
            if (handler == null) {
                logger.debug("msg is not for this feature");
                return true;
            }
            if (!handler.isDuplicate(msg)) {
                if (handler.matchesGroup(group) && handler.matches(msg)) {
                    logger.debug("{}:{}->{} cmd1:{} group {}/{}", feature.getDevice().getAddress(), feature.getName(),
                            handler.getClass().getSimpleName(), HexUtils.getHexString(cmd1), group, handler.getGroup());
                    handler.handleMessage(group, cmd1, msg, feature);
                } else {
                    logger.debug("message ignored because matches group: {} matches filter: {}",
                            handler.matchesGroup(group), handler.matches(msg));
                }
            } else {
                logger.debug("message ignored as duplicate. Matches group: {} matches filter: {}",
                        handler.matchesGroup(group), handler.matches(msg));
            }
        } catch (FieldException e) {
            logger.warn("couldn't parse ALL_LINK message: {}", msg, e);
        }
        return true;
    }

    /**
     * Checks if this message is in response to previous query by this feature
     *
     * @param msg
     * @return true;
     */
    boolean isMyDirectAck(Msg msg) {
        return msg.isAckOfDirect() && (feature.getQueryStatus() == LegacyDeviceFeature.QueryStatus.QUERY_PENDING)
                && feature.equals(feature.getDevice().getFeatureQueried());
    }

    /**
     * Dispatches message
     *
     * @param msg Message to dispatch
     * @return true if this message was found to be a reply to a direct message,
     *         and was claimed by one of the handlers
     */
    public abstract boolean dispatch(Msg msg);

    public static class DefaultDispatcher extends LegacyMessageDispatcher {
        DefaultDispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            byte cmd = 0x00;
            byte cmd1 = 0x00;
            boolean isConsumed = false;
            int key = -1;
            try {
                cmd = msg.getByte("Cmd");
                cmd1 = msg.getByte("command1");
            } catch (FieldException e) {
                logger.debug("no command found, dropping msg {}", msg);
                return false;
            }
            if (msg.isAllLinkCleanupAckOrNack()) {
                // Had cases when a KeypadLinc would send an ALL_LINK_CLEANUP_ACK
                // in response to a direct status query message
                return false;
            }
            if (handleAllLinkMessage(msg)) {
                return false;
            }
            if (msg.isAckOfDirect()) {
                // in the case of direct ack, the cmd1 code is useless.
                // you have to know what message was sent before to
                // interpret the reply message
                if (isMyDirectAck(msg)) {
                    logger.debug("{}:{} DIRECT_ACK: q:{} cmd: {}", feature.getDevice().getAddress(), feature.getName(),
                            feature.getQueryStatus(), cmd);
                    isConsumed = true;
                    if (cmd == 0x50) {
                        // must be a reply to our message, tweak the cmd1 code!
                        logger.debug("changing key to 0x19 for msg {}", msg);
                        key = 0x19; // we have installed a handler under that command number
                    }
                }
            } else {
                key = (cmd1 & 0xFF);
            }
            if (key != -1 || feature.isStatusFeature()) {
                LegacyMessageHandler handler = feature.getMsgHandlers().get(key);
                if (handler == null) {
                    handler = feature.getDefaultMsgHandler();
                }
                if (handler.matches(msg)) {
                    if (!isConsumed) {
                        logger.debug("{}:{}->{} DIRECT", feature.getDevice().getAddress(), feature.getName(),
                                handler.getClass().getSimpleName());
                    }
                    handler.handleMessage(-1, cmd1, msg, feature);
                }
            }
            if (isConsumed) {
                feature.setQueryStatus(LegacyDeviceFeature.QueryStatus.QUERY_ANSWERED);
                logger.debug("defdisp: {}:{} set status to: {}", feature.getDevice().getAddress(), feature.getName(),
                        feature.getQueryStatus());
            }
            return isConsumed;
        }
    }

    public static class DefaultGroupDispatcher extends LegacyMessageDispatcher {
        DefaultGroupDispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            byte cmd = 0x00;
            byte cmd1 = 0x00;
            boolean isConsumed = false;
            int key = -1;
            try {
                cmd = msg.getByte("Cmd");
                cmd1 = msg.getByte("command1");
            } catch (FieldException e) {
                logger.debug("no command found, dropping msg {}", msg);
                return false;
            }
            if (msg.isAllLinkCleanupAckOrNack()) {
                // Had cases when a KeypadLinc would send an ALL_LINK_CLEANUP_ACK
                // in response to a direct status query message
                return false;
            }
            if (handleAllLinkMessage(msg)) {
                return false;
            }
            if (msg.isAckOfDirect()) {
                // in the case of direct ack, the cmd1 code is useless.
                // you have to know what message was sent before to
                // interpret the reply message
                if (isMyDirectAck(msg)) {
                    logger.debug("{}:{} qs:{} cmd: {}", feature.getDevice().getAddress(), feature.getName(),
                            feature.getQueryStatus(), cmd);
                    isConsumed = true;
                    if (cmd == 0x50) {
                        // must be a reply to our message, tweak the cmd1 code!
                        logger.debug("changing key to 0x19 for msg {}", msg);
                        key = 0x19; // we have installed a handler under that command number
                    }
                }
            } else {
                key = (cmd1 & 0xFF);
            }
            if (key != -1) {
                for (LegacyDeviceFeature connectedFeature : feature.getConnectedFeatures()) {
                    LegacyMessageHandler handler = connectedFeature.getMsgHandlers().get(key);
                    if (handler == null) {
                        handler = connectedFeature.getDefaultMsgHandler();
                    }
                    if (handler.matches(msg)) {
                        if (!isConsumed) {
                            logger.debug("{}:{}->{} DIRECT", connectedFeature.getDevice().getAddress(),
                                    connectedFeature.getName(), handler.getClass().getSimpleName());
                        }
                        handler.handleMessage(-1, cmd1, msg, connectedFeature);
                    }

                }
            }
            if (isConsumed) {
                feature.setQueryStatus(LegacyDeviceFeature.QueryStatus.QUERY_ANSWERED);
                logger.debug("{}:{} set status to: {}", feature.getDevice().getAddress(), feature.getName(),
                        feature.getQueryStatus());
            }
            return isConsumed;
        }
    }

    public static class PollGroupDispatcher extends LegacyMessageDispatcher {
        PollGroupDispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            if (msg.isAllLinkCleanupAckOrNack()) {
                // Had cases when a KeypadLinc would send an ALL_LINK_CLEANUP_ACK
                // in response to a direct status query message
                return false;
            }
            if (handleAllLinkMessage(msg)) {
                return false;
            }
            if (msg.isAckOfDirect()) {
                boolean isMyAck = isMyDirectAck(msg);
                if (isMyAck) {
                    logger.debug("{}:{} got poll ACK", feature.getDevice().getAddress(), feature.getName());
                }
                return isMyAck;
            }
            return false; // not a direct ack, so we didn't consume it either
        }
    }

    public static class SimpleDispatcher extends LegacyMessageDispatcher {
        SimpleDispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            byte cmd1 = 0x00;
            try {
                if (handleAllLinkMessage(msg)) {
                    return false;
                }
                if (msg.isAllLinkCleanupAckOrNack()) {
                    // Had cases when a KeypadLinc would send an ALL_LINK_CLEANUP_ACK
                    // in response to a direct status query message
                    return false;
                }
                cmd1 = msg.getByte("command1");
            } catch (FieldException e) {
                logger.debug("no cmd1 found, dropping msg {}", msg);
                return false;
            }
            boolean isConsumed = isMyDirectAck(msg);
            int key = (cmd1 & 0xFF);
            LegacyMessageHandler handler = feature.getMsgHandlers().get(key);
            if (handler == null) {
                handler = feature.getDefaultMsgHandler();
            }
            if (handler.matches(msg)) {
                logger.trace("{}:{}->{} {}", feature.getDevice().getAddress(), feature.getName(),
                        handler.getClass().getSimpleName(), msg);
                handler.handleMessage(-1, cmd1, msg, feature);
            }
            return isConsumed;
        }
    }

    public static class X10Dispatcher extends LegacyMessageDispatcher {
        X10Dispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            try {
                byte rawX10 = msg.getByte("rawX10");
                int cmd = (rawX10 & 0x0f);
                LegacyMessageHandler handler = feature.getMsgHandlers().get(cmd);
                if (handler == null) {
                    handler = feature.getDefaultMsgHandler();
                }
                logger.debug("{}:{}->{} {}", feature.getDevice().getAddress(), feature.getName(),
                        handler.getClass().getSimpleName(), msg);
                if (handler.matches(msg)) {
                    handler.handleMessage(-1, (byte) cmd, msg, feature);
                }
            } catch (FieldException e) {
                logger.warn("error parsing {}: ", msg, e);
            }
            return false;
        }
    }

    public static class PassThroughDispatcher extends LegacyMessageDispatcher {
        PassThroughDispatcher(LegacyDeviceFeature feature) {
            super(feature);
        }

        @Override
        public boolean dispatch(Msg msg) {
            LegacyMessageHandler handler = feature.getDefaultMsgHandler();
            if (handler.matches(msg)) {
                logger.trace("{}:{}->{} {}", feature.getDevice().getAddress(), feature.getName(),
                        handler.getClass().getSimpleName(), msg);
                handler.handleMessage(-1, (byte) 0x01, msg, feature);
            }
            return false;
        }
    }

    /**
     * Drop all incoming messages silently
     */
    public static class NoOpDispatcher extends LegacyMessageDispatcher {
        NoOpDispatcher(LegacyDeviceFeature feature) {
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
     * @param params
     * @param feature the feature for which to create the dispatcher
     * @return the handler which was created
     */
    @Nullable
    public static <T extends LegacyMessageDispatcher> T makeHandler(String name, Map<String, String> params,
            LegacyDeviceFeature feature) {
        try {
            String className = LegacyMessageDispatcher.class.getName() + "$" + name;
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
