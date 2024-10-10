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
 * @author Jeremy Setton - Rewrite insteon binding
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
        LegacyDeviceFeature feature = new LegacyDeviceFeature(name);
        feature.setStatusFeature(isStatus);
        feature.setTimeout(timeout);
        HandlerEntry dispatcher = this.dispatcher;
        if (dispatcher != null) {
            LegacyMessageDispatcher handler = LegacyMessageDispatcher.makeHandler(dispatcher.getName(),
                    dispatcher.getParameters(), feature);
            if (handler != null) {
                feature.setMessageDispatcher(handler);
            }
        }
        HandlerEntry pollHandler = this.pollHandler;
        if (pollHandler != null) {
            LegacyPollHandler handler = LegacyPollHandler.makeHandler(pollHandler.getName(),
                    pollHandler.getParameters(), feature);
            if (handler != null) {
                feature.setPollHandler(handler);
            }
        }
        HandlerEntry defaultCmdHandler = this.defaultCmdHandler;
        if (defaultCmdHandler != null) {
            LegacyCommandHandler handler = LegacyCommandHandler.makeHandler(defaultCmdHandler.getName(),
                    defaultCmdHandler.getParameters(), feature);
            if (handler != null) {
                feature.setDefaultCommandHandler(handler);
            }
        }
        HandlerEntry defaultMsgHandler = this.defaultMsgHandler;
        if (defaultMsgHandler != null) {
            LegacyMessageHandler handler = LegacyMessageHandler.makeHandler(defaultMsgHandler.getName(),
                    defaultMsgHandler.getParameters(), feature);
            if (handler != null) {
                feature.setDefaultMsgHandler(handler);
            }
        }
        for (Entry<Integer, HandlerEntry> entry : messageHandlers.entrySet()) {
            LegacyMessageHandler handler = LegacyMessageHandler.makeHandler(entry.getValue().getName(),
                    entry.getValue().getParameters(), feature);
            if (handler != null) {
                feature.addMessageHandler(entry.getKey(), handler);
            }
        }
        for (Entry<Class<? extends Command>, HandlerEntry> entry : commandHandlers.entrySet()) {
            LegacyCommandHandler handler = LegacyCommandHandler.makeHandler(entry.getValue().getName(),
                    entry.getValue().getParameters(), feature);
            if (handler != null) {
                feature.addCommandHandler(entry.getKey(), handler);
            }
        }
        return feature;
    }

    @Override
    public String toString() {
        return getName() + "(" + isStatusFeature() + ")";
    }
}
