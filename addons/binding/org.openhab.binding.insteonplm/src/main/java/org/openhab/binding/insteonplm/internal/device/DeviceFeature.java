/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.util.HashMap;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.commands.NoOpCommandHandler;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DeviceFeature represents a certain feature (trait) of an insteon device. This feature is shared
 * over multiple things and the thing handler is passed in to use to make updates.
 *
 * The DeviceFeature does the processing of incoming messages, and handles commands for the
 * particular feature it represents.
 *
 * It uses four mechanisms for that:
 *
 * 1) MessageDispatcher: makes high level decisions about an incoming message and then runs the
 * 2) MessageHandler: further processes the message, updates state etc
 * 3) CommandHandler: translates commands from the openhab bus into an Insteon message.
 * 4) PollHandler: creates an Insteon message to query the DeviceFeature
 *
 * Lastly, DeviceFeatureListeners can register with the DeviceFeature to get notifications when
 * the state of a feature has changed. In practice, a DeviceFeatureListener corresponds to an
 * OpenHAB item.
 *
 * The character of a DeviceFeature is thus given by a set of message and command handlers.
 * A FeatureTemplate captures exactly that: it says what set of handlers make up a DeviceFeature.
 *
 * DeviceFeatures are added to a new device by referencing a FeatureTemplate (defined in device_features.xml)
 * from the Device definition file (device_types.xml).
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 * @since 1.5.0
 */

public class DeviceFeature {
    public static enum QueryStatus {
        NEVER_QUERIED,
        QUERY_PENDING,
        QUERY_ANSWERED
    }

    private static final Logger logger = LoggerFactory.getLogger(DeviceFeature.class);

    private String m_name = "INVALID_FEATURE_NAME";
    private int m_directAckTimeout = 6000;
    private QueryStatus m_queryStatus = QueryStatus.NEVER_QUERIED;

    private MessageHandler m_defaultMsgHandler = new MessageHandler.DefaultMsgHandler(this);
    private CommandHandler m_defaultCommandHandler = new NoOpCommandHandler(this);
    private PollHandler m_pollHandler;
    private boolean m_statusFeature = false;

    private HashMap<Integer, MessageHandler> m_msgHandlers = new HashMap<Integer, MessageHandler>();
    private HashMap<Class<? extends Command>, CommandHandler> m_commandHandlers = new HashMap<Class<? extends Command>, CommandHandler>();

    /**
     * Constructor
     *
     * @param name descriptive name for that feature
     */
    public DeviceFeature(String name) {
        m_name = name;
    }

    // various simple getters
    public String getName() {
        return m_name;
    }

    public synchronized QueryStatus getQueryStatus() {
        return m_queryStatus;
    }

    public int getDirectAckTimeout() {
        return m_directAckTimeout;
    }

    public MessageHandler getDefaultMsgHandler() {
        return m_defaultMsgHandler;
    }

    public PollHandler getPollHandler() {
        return m_pollHandler;
    }

    public HashMap<Integer, MessageHandler> getMsgHandlers() {
        return this.m_msgHandlers;
    }

    public boolean isStatusFeature() {
        return m_statusFeature;
    }

    // various simple setters
    public void setStatusFeature(boolean state) {
        m_statusFeature = state;
    }

    public void setMessageDispatcher(MessageDispatcher md) {
        m_dispatcher = md;
    }

    public void setDefaultCommandHandler(CommandHandler ch) {
        m_defaultCommandHandler = ch;
    }

    public void setDefaultMsgHandler(MessageHandler mh) {
        m_defaultMsgHandler = mh;
    }

    public synchronized void setQueryStatus(QueryStatus status) {
        logger.trace("{} set query status to: {}", m_name, status);
        m_queryStatus = status;
    }

    public void setTimeout(String s) {
        if (s != null && !s.isEmpty()) {
            try {
                m_directAckTimeout = Integer.parseInt(s);
                logger.trace("ack timeout set to {}", m_directAckTimeout);
            } catch (NumberFormatException e) {
                logger.error("invalid number for timeout: {}", s);
            }
        }
    }

    /**
     * Called when message is incoming. Dispatches message according to message dispatcher
     *
     * @param msg The message to dispatch
     * @param port the port from which the message came
     * @return true if dispatch successful
     */
    public boolean handleMessage(InsteonThingHandler handler, Message msg) {
        if (m_dispatcher == null) {
            logger.error("{} no dispatcher for msg {}", m_name, msg);
            return false;
        }
        return (m_dispatcher.dispatch(handler, msg));
    }

    /**
     * Called when an openhab command arrives for this device feature
     *
     * @param c the channel the command is on
     * @param cmd the command to be exectued
     */
    public void handleCommand(InsteonThingHandler handler, ChannelUID c, Command cmd) {
        Class<? extends Command> key = cmd.getClass();
        CommandHandler h = m_commandHandlers.containsKey(key) ? m_commandHandlers.get(key) : m_defaultCommandHandler;
        logger.trace("{} uses {} to handle command {} for {}", getName(), h.getClass().getSimpleName(),
                key.getSimpleName(), handler.getAddress());
        h.handleCommand(handler, c, cmd);
    }

    /**
     * Make a poll message using the configured poll message handler
     *
     * @return the poll message
     *         public Message makePollMsg() {
     *         if (m_pollHandler == null) {
     *         return null;
     *         }
     *         logger.trace("{} making poll msg for {} using handler {}", getName(), getDevice().getAddress(),
     *         m_pollHandler.getClass().getSimpleName());
     *         Message m = m_pollHandler.makeMsg(m_device);
     *         return m;
     *         }
     */

    /**
     * Adds a message handler to this device feature.
     *
     * @param cm1 The insteon cmd1 of the incoming message for which the handler should be used
     * @param handler the handler to invoke
     */
    public void addMessageHandler(int cm1, MessageHandler handler) {
        synchronized (m_msgHandlers) {
            m_msgHandlers.put(cm1, handler);
        }
    }

    /**
     * Adds a command handler to this device feature
     *
     * @param c the command for which this handler is invoked
     * @param handler the handler to call
     */
    public void addCommandHandler(Class<? extends Command> c, CommandHandler handler) {
        synchronized (m_commandHandlers) {
            m_commandHandlers.put(c, handler);
        }
    }

    /**
     * Turn DeviceFeature into String
     */
    @Override
    public String toString() {
        return m_name + " (" + m_commandHandlers.size() + ":" + m_msgHandlers.size() + ")";
    }

    /** Sets the poll handler to use for making poll messages. */
    public void setPollHandler(PollHandler pollHandler) {
        m_pollHandler = pollHandler;
    }

    /**
     * Make a poll message using the configured poll message handler
     *
     * @return the poll message
     */
    public Message makePollMsg(InsteonThingHandler handler) {
        if (m_pollHandler == null) {
            return null;
        }
        logger.trace("{} making poll msg for {} using handler {}", getName(), handler.getAddress(),
                m_pollHandler.getClass().getSimpleName());
        Message m = m_pollHandler.makeMsg(handler);
        return m;
    }
}
