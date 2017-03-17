/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.commands.NoOpCommandHandler;
import org.openhab.binding.insteonplm.internal.device.messages.DefaultMsgHandler;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
    private static final Logger logger = LoggerFactory.getLogger(DeviceFeature.class);

    private String m_name = "INVALID_FEATURE_NAME";
    private int m_directAckTimeout = 6000;

    private MessageHandler m_defaultMsgHandler = new DefaultMsgHandler(this);
    private CommandHandler m_defaultCommandHandler = new NoOpCommandHandler(this);

    private Map<StandardInsteonMessages, List<MessageHandler>> m_msgHandlers = Maps.newHashMap();
    private Map<Class<? extends Command>, CommandHandler> m_commandHandlers = Maps.newHashMap();

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

    public int getDirectAckTimeout() {
        return m_directAckTimeout;
    }

    public MessageHandler getDefaultMsgHandler() {
        return m_defaultMsgHandler;
    }

    public Map<StandardInsteonMessages, List<MessageHandler>> getMsgHandlers() {
        return this.m_msgHandlers;
    }

    // various simple setters
    public void setDefaultCommandHandler(CommandHandler ch) {
        m_defaultCommandHandler = ch;
    }

    public void setDefaultMsgHandler(MessageHandler mh) {
        m_defaultMsgHandler = mh;
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
     * Adds a message handler to this device feature.
     *
     * @param cm1 The insteon cmd1 of the incoming message for which the handler should be used
     * @param handler the handler to invoke
     */
    public void addMessageHandler(StandardInsteonMessages cm1, MessageHandler handler) {
        synchronized (m_msgHandlers) {
            List<MessageHandler> handlers = m_msgHandlers.get(cm1);
            if (handlers == null) {
                handlers = Lists.newArrayList();
                m_msgHandlers.put(cm1, handlers);
            }
            handlers.add(handler);
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

    public void handleGroupMessage(InsteonThingHandler insteonThingHandler, int group, byte command,
            StandardMessageReceived message, Channel channel) {
    }
}
