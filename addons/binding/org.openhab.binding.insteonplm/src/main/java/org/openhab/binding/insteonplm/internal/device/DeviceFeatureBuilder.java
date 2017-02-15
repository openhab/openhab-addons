/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.device;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class which contains the basic info needed to create a device feature.
 * Here, all handlers are represented as strings. The actual device feature
 * is then instantiated from the template by calling the build() function.
 *
 * @author Daniel Pfrommer
 * @since 1.5.0
 */
public class DeviceFeatureBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DeviceFeatureBuilder.class);
    private static final String COMMAND_HANDLER_PACKAGE = "org.openhab.binding.insteonplm.internal.device.commands";
    private static final String MESSAGE_HANDLER_PACKAGE = "org.openhab.binding.insteonplm.internal.device.commands.MessageHandler";
    private String m_name = null;
    private String m_timeout = null;
    private boolean m_isStatus = false;
    private HandlerEntry m_dispatcher = null;
    private HandlerEntry m_pollHandler = null;
    private HandlerEntry m_defaultMsgHandler = null;
    private HandlerEntry m_defaultCmdHandler = null;
    private HashMap<Integer, HandlerEntry> m_messageHandlers = new HashMap<Integer, HandlerEntry>();
    private HashMap<Class<? extends Command>, HandlerEntry> m_commandHandlers = new HashMap<Class<? extends Command>, HandlerEntry>();

    // simple getters
    public String getName() {
        return m_name;
    }

    public String getTimeout() {
        return m_timeout;
    }

    public boolean isStatusFeature() {
        return m_isStatus;
    }

    public HandlerEntry getPollHandler() {
        return m_pollHandler;
    }

    public HandlerEntry getDispatcher() {
        return m_dispatcher;
    }

    public HandlerEntry getDefaultCommandHandler() {
        return m_defaultCmdHandler;
    }

    public HandlerEntry getDefaultMessageHandler() {
        return m_defaultMsgHandler;
    }

    /**
     * Retrieves a hashmap of message command code to command handler name
     *
     * @return a Hashmap from Integer to String representing the command codes and the associated message handlers
     */
    public HashMap<Integer, HandlerEntry> getMessageHandlers() {
        return m_messageHandlers;
    }

    /**
     * Similar to getMessageHandlers(), but for command handlers
     * Instead of Integers it uses the class of the Command as a key
     *
     * @see #getMessageHandlers()
     * @return a HashMap from Command Classes to CommandHandler names
     */
    public HashMap<Class<? extends Command>, HandlerEntry> getCommandHandlers() {
        return m_commandHandlers;
    }

    // simple setters
    public void setName(String name) {
        m_name = name;
    }

    public void setStatusFeature(boolean status) {
        m_isStatus = status;
    }

    public void setTimeout(String s) {
        m_timeout = s;
    }

    public void setMessageDispatcher(HandlerEntry he) {
        m_dispatcher = he;
    }

    public void setPollHandler(HandlerEntry he) {
        m_pollHandler = he;
    }

    public void setDefaultCommandHandler(HandlerEntry cmd) {
        m_defaultCmdHandler = cmd;
    }

    public void setDefaultMessageHandler(HandlerEntry he) {
        m_defaultMsgHandler = he;
    }

    /**
     * Adds a message handler mapped from the command which this handler should be invoked for
     * to the name of the handler to be created
     *
     * @param cmd command to be mapped
     * @param he handler entry to map to
     */
    public void addMessageHandler(int cmd, HandlerEntry he) {
        m_messageHandlers.put(cmd, he);
    }

    /**
     * Adds a command handler mapped from the command class which this handler should be invoke for
     * to the name of the handler to be created
     */
    public void addCommandHandler(Class<? extends Command> command, HandlerEntry he) {
        m_commandHandlers.put(command, he);
    }

    /**
     * Method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param params
     * @param f the feature for which to create the handler
     * @return the handler which was created
     */
    private <T extends CommandHandler> T makeCommandHandler(String name, HashMap<String, String> params,
            DeviceFeature f) {
        String cname = COMMAND_HANDLER_PACKAGE + "$" + name;
        try {
            Class<?> c = Class.forName(cname);
            @SuppressWarnings("unchecked")
            Class<? extends T> dc = (Class<? extends T>) c;
            T ch = dc.getDeclaredConstructor(DeviceFeature.class).newInstance(f);

            // Call sets for the properties out of the parameters.
            for (String paramName : params.keySet()) {
                Method method = dc.getMethod(
                        "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1), String.class);
                if (method == null) {
                    logger.error("Unable to find method {} on {}", "set" + paramName, name);
                } else {
                    method.invoke(ch, params.get(paramName));
                }
            }
            return ch;
        } catch (Exception e) {
            logger.error("error trying to create message handler: {}", name, e);
        }
        return null;
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param name the name of the handler to create
     * @param params
     * @param f the feature for which to create the handler
     * @return the handler which was created
     */
    private <T extends MessageHandler> T makeMessageHandler(String name, HashMap<String, String> params,
            DeviceFeature f) {
        String cname = MESSAGE_HANDLER_PACKAGE + "$" + name;
        try {
            Class<?> c = Class.forName(cname);
            @SuppressWarnings("unchecked")
            Class<? extends T> dc = (Class<? extends T>) c;
            T mh = dc.getDeclaredConstructor(DeviceFeature.class).newInstance(f);
            // Call sets for the properties out of the parameters.
            for (String paramName : params.keySet()) {
                Method method = dc.getMethod(
                        "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1), String.class);
                if (method == null) {
                    logger.error("Unable to find method {} on {}", "set" + paramName, name);
                } else {
                    method.invoke(mh, params.get(paramName));
                }
            }
            return mh;
        } catch (Exception e) {
            logger.error("error trying to create message handler: {}", name, e);
        }
        return null;
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param ph the name of the handler to create
     * @param f the feature for which to create the handler
     * @return the handler which was created
     */
    private <T extends PollHandler> T makePollHandler(HandlerEntry ph, DeviceFeature f) {
        String cname = PollHandler.class.getName() + "$" + ph.getName();
        try {
            Class<?> c = Class.forName(cname);
            @SuppressWarnings("unchecked")
            Class<? extends T> dc = (Class<? extends T>) c;
            T phc = dc.getDeclaredConstructor(DeviceFeature.class).newInstance(f);
            phc.setParameters(ph.getParams());
            // Call sets for the properties out of the parameters.
            for (String paramName : ph.getParams().keySet()) {
                Method method = dc.getMethod(
                        "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1), String.class);
                if (method == null) {
                    logger.error("Unable to find method {} on {}", "set" + paramName, ph.getName());
                } else {
                    method.invoke(phc, ph.getParams().get(paramName));
                }
            }
            return phc;
        } catch (Exception e) {
            logger.error("error trying to create message handler: {}", ph.getName(), e);
        }
        return null;
    }

    /**
     * Builds the actual feature
     *
     * @return the feature which this template describes
     */
    public DeviceFeature build() {
        DeviceFeature f = new DeviceFeature(m_name);
        f.setStatusFeature(m_isStatus);
        f.setTimeout(m_timeout);
        if (m_pollHandler != null) {
            f.setPollHandler(makePollHandler(m_pollHandler, f));
        }
        if (m_defaultCmdHandler != null) {
            f.setDefaultCommandHandler(
                    makeCommandHandler(m_defaultCmdHandler.getName(), m_defaultCmdHandler.getParams(), f));
        }
        if (m_defaultMsgHandler != null) {
            f.setDefaultMsgHandler(
                    makeMessageHandler(m_defaultMsgHandler.getName(), m_defaultMsgHandler.getParams(), f));
        }
        for (Entry<Integer, HandlerEntry> mH : m_messageHandlers.entrySet()) {
            f.addMessageHandler(mH.getKey(), makeMessageHandler(mH.getValue().getName(), mH.getValue().getParams(), f));
        }
        for (Entry<Class<? extends Command>, HandlerEntry> cH : m_commandHandlers.entrySet()) {
            f.addCommandHandler(cH.getKey(), makeCommandHandler(cH.getValue().getName(), cH.getValue().getParams(), f));
        }
        return f;
    }

    @Override
    public String toString() {
        return getName() + "(" + isStatusFeature() + ")";
    }
}
