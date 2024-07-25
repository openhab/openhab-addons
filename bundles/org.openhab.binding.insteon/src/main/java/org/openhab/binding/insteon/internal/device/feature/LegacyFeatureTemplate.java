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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.LegacyDeviceFeature;
import org.openhab.core.types.Command;

/**
 * A simple class which contains the basic info needed to create a device feature.
 * Here, all handlers are represented as strings. The actual device feature
 * is then instantiated from the template by calling the build() function.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class LegacyFeatureTemplate {
    private String name;
    private String timeout;
    private boolean isStatus;
    private @Nullable HandlerEntry dispatcher = null;
    private @Nullable HandlerEntry pollHandler = null;
    private @Nullable HandlerEntry defaultMsgHandler = null;
    private @Nullable HandlerEntry defaultCmdHandler = null;
    private Map<Integer, HandlerEntry> messageHandlers = new HashMap<>();
    private Map<Class<? extends Command>, HandlerEntry> commandHandlers = new HashMap<>();

    public LegacyFeatureTemplate(String name, boolean isStatus, String timeout) {
        this.name = name;
        this.isStatus = isStatus;
        this.timeout = timeout;
    }

    // simple getters
    public String getName() {
        return name;
    }

    public String getTimeout() {
        return timeout;
    }

    public boolean isStatusFeature() {
        return isStatus;
    }

    public @Nullable HandlerEntry getPollHandler() {
        return pollHandler;
    }

    public @Nullable HandlerEntry getDispatcher() {
        return dispatcher;
    }

    public @Nullable HandlerEntry getDefaultCommandHandler() {
        return defaultCmdHandler;
    }

    public @Nullable HandlerEntry getDefaultMessageHandler() {
        return defaultMsgHandler;
    }

    /**
     * Retrieves a hashmap of message command code to command handler name
     *
     * @return a Hashmap from Integer to String representing the command codes and the associated message handlers
     */
    public Map<Integer, HandlerEntry> getMessageHandlers() {
        return messageHandlers;
    }

    /**
     * Similar to getMessageHandlers(), but for command handlers
     * Instead of Integers it uses the class of the Command as a key
     *
     * @see #getMessageHandlers()
     * @return a HashMap from Command Classes to CommandHandler names
     */
    public Map<Class<? extends Command>, HandlerEntry> getCommandHandlers() {
        return commandHandlers;
    }

    // simple setters

    public void setMessageDispatcher(HandlerEntry he) {
        dispatcher = he;
    }

    public void setPollHandler(HandlerEntry he) {
        pollHandler = he;
    }

    public void setDefaultCommandHandler(HandlerEntry cmd) {
        defaultCmdHandler = cmd;
    }

    public void setDefaultMessageHandler(HandlerEntry he) {
        defaultMsgHandler = he;
    }

    /**
     * Adds a message handler mapped from the command which this handler should be invoked for
     * to the name of the handler to be created
     *
     * @param cmd command to be mapped
     * @param he handler entry to map to
     */
    public void addMessageHandler(int cmd, HandlerEntry he) {
        messageHandlers.put(cmd, he);
    }

    /**
     * Adds a command handler mapped from the command class which this handler should be invoke for
     * to the name of the handler to be created
     */
    public void addCommandHandler(Class<? extends Command> command, HandlerEntry he) {
        commandHandlers.put(command, he);
    }

    /**
     * Builds the actual feature
     *
     * @return the feature which this template describes
     */
    public LegacyDeviceFeature build() {
        LegacyDeviceFeature f = new LegacyDeviceFeature(name);
        f.setStatusFeature(isStatus);
        f.setTimeout(timeout);
        HandlerEntry dispatcher = this.dispatcher;
        if (dispatcher != null) {
            LegacyMessageDispatcher h = LegacyMessageDispatcher.makeHandler(dispatcher.getName(),
                    dispatcher.getParams(), f);
            if (h != null) {
                f.setMessageDispatcher(h);
            }
        }
        HandlerEntry pollHandler = this.pollHandler;
        if (pollHandler != null) {
            LegacyPollHandler h = LegacyPollHandler.makeHandler(pollHandler, f);
            if (h != null) {
                f.setPollHandler(h);
            }
        }
        HandlerEntry defaultCmdHandler = this.defaultCmdHandler;
        if (defaultCmdHandler != null) {
            LegacyCommandHandler h = LegacyCommandHandler.makeHandler(defaultCmdHandler.getName(),
                    defaultCmdHandler.getParams(), f);
            if (h != null) {
                f.setDefaultCommandHandler(h);
            }
        }
        HandlerEntry defaultMsgHandler = this.defaultMsgHandler;
        if (defaultMsgHandler != null) {
            LegacyMessageHandler h = LegacyMessageHandler.makeHandler(defaultMsgHandler.getName(),
                    defaultMsgHandler.getParams(), f);
            if (h != null) {
                f.setDefaultMsgHandler(h);
            }
        }
        for (Entry<Integer, HandlerEntry> mH : messageHandlers.entrySet()) {
            LegacyMessageHandler h = LegacyMessageHandler.makeHandler(mH.getValue().getName(),
                    mH.getValue().getParams(), f);
            if (h != null) {
                f.addMessageHandler(mH.getKey(), h);
            }
        }
        for (Entry<Class<? extends Command>, HandlerEntry> cH : commandHandlers.entrySet()) {
            LegacyCommandHandler h = LegacyCommandHandler.makeHandler(cH.getValue().getName(),
                    cH.getValue().getParams(), f);
            if (h != null) {
                f.addCommandHandler(cH.getKey(), h);
            }
        }
        return f;
    }

    @Override
    public String toString() {
        return getName() + "(" + isStatusFeature() + ")";
    }
}
