/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.handler.feature.CommandHandler;
import org.openhab.binding.insteon.internal.handler.feature.MessageDispatcher;
import org.openhab.binding.insteon.internal.handler.feature.MessageHandler;
import org.openhab.binding.insteon.internal.handler.feature.PollHandler;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.openhab.core.types.Command;

/**
 * A simple class which contains the basic info needed to create a device feature.
 * Here, all handlers are represented as strings. The actual device feature
 * is then instantiated from the template by calling the build() function.
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class FeatureTemplate {
    private String type;
    private @Nullable HandlerEntry dispatcher;
    private @Nullable HandlerEntry pollHandler;
    private @Nullable HandlerEntry defaultMsgHandler;
    private @Nullable HandlerEntry defaultCmdHandler;
    private Map<Integer, HandlerEntry> messageHandlers = new HashMap<>();
    private Map<Class<? extends Command>, HandlerEntry> commandHandlers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();

    public FeatureTemplate(String type, Map<String, String> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public void setMessageDispatcher(HandlerEntry dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setPollHandler(HandlerEntry pollHandler) {
        this.pollHandler = pollHandler;
    }

    public void setDefaultCommandHandler(HandlerEntry defaultCmdHandler) {
        this.defaultCmdHandler = defaultCmdHandler;
    }

    public void setDefaultMessageHandler(HandlerEntry defaultMsgHandler) {
        this.defaultMsgHandler = defaultMsgHandler;
    }

    /**
     * Adds a message handler to this feature template
     *
     * @param command the insteon command to be mapped
     * @param msgHandler the message handler entry to map to
     */
    public void addMessageHandler(int command, HandlerEntry msgHandler) {
        messageHandlers.put(command, msgHandler);
    }

    /**
     * Adds a command handler to this feature template
     *
     * @param classRef the command class reference to be mapped
     * @param cmdHandler the command handler entry to map to
     */
    public void addCommandHandler(Class<? extends Command> classRef, HandlerEntry cmdHandler) {
        commandHandlers.put(classRef, cmdHandler);
    }

    /**
     * Builds the actual feature
     *
     * @return the feature which this template describes
     */
    public DeviceFeature build() {
        DeviceFeature feature = new DeviceFeature(type);
        // add feature template parameters
        feature.addParameters(parameters);

        HandlerEntry dispatcher = this.dispatcher;
        if (dispatcher != null) {
            MessageDispatcher handler = MessageDispatcher.makeHandler(dispatcher.getName(), dispatcher.getParameters(),
                    feature);
            if (handler != null) {
                feature.setMessageDispatcher(handler);
            }
        }

        HandlerEntry pollHandler = this.pollHandler;
        if (pollHandler != null) {
            PollHandler handler = PollHandler.makeHandler(pollHandler.getName(), pollHandler.getParameters(), feature);
            if (handler != null) {
                feature.setPollHandler(handler);
            }
        }

        HandlerEntry defaultCmdHandler = this.defaultCmdHandler;
        if (defaultCmdHandler != null) {
            CommandHandler handler = CommandHandler.makeHandler(defaultCmdHandler.getName(),
                    defaultCmdHandler.getParameters(), feature);
            if (handler != null) {
                feature.setDefaultCommandHandler(handler);
            }
        }

        HandlerEntry defaultMsgHandler = this.defaultMsgHandler;
        if (defaultMsgHandler != null) {
            MessageHandler handler = MessageHandler.makeHandler(defaultMsgHandler.getName(),
                    defaultMsgHandler.getParameters(), feature);
            if (handler != null) {
                feature.setDefaultMsgHandler(handler);
            }
        }

        for (Entry<Integer, HandlerEntry> msgHandler : messageHandlers.entrySet()) {
            MessageHandler handler = MessageHandler.makeHandler(msgHandler.getValue().getName(),
                    msgHandler.getValue().getParameters(), feature);
            if (handler != null) {
                feature.addMessageHandler(msgHandler.getKey(), handler);
            }
        }

        for (Entry<Class<? extends Command>, HandlerEntry> cmdHandler : commandHandlers.entrySet()) {
            CommandHandler handler = CommandHandler.makeHandler(cmdHandler.getValue().getName(),
                    cmdHandler.getValue().getParameters(), feature);
            if (handler != null) {
                feature.addCommandHandler(cmdHandler.getKey(), handler);
            }
        }

        return feature;
    }

    @Override
    public String toString() {
        String s = "type:" + type;
        if (!parameters.isEmpty()) {
            s += "|parameters:" + parameters.entrySet().stream().map(Entry::toString).collect(Collectors.joining(","));
        }
        if (dispatcher != null) {
            s += "|dispatcher:" + dispatcher;
        }
        if (dispatcher != null) {
            s += "|pollHandler:" + pollHandler;
        }
        if (defaultMsgHandler != null) {
            s += "|defaultMsgHandler:" + defaultMsgHandler;
        }
        if (defaultCmdHandler != null) {
            s += "|defaultCmdHandler:" + defaultCmdHandler;
        }
        if (!messageHandlers.isEmpty()) {
            s += "|msgHandlers:" + messageHandlers.entrySet().stream()
                    .map(mH -> String.format("%s->%s", ByteUtils.getHexString(mH.getKey()), mH.getValue()))
                    .collect(Collectors.joining(","));
        }
        if (!commandHandlers.isEmpty()) {
            s += "|cmdHandlers:" + commandHandlers.entrySet().stream()
                    .map(cH -> String.format("%s->%s", cH.getKey().getSimpleName(), cH.getValue()))
                    .collect(Collectors.joining(","));
        }
        return s;
    }

    /**
     * Class that reflects handler entry
     */
    public static class HandlerEntry {
        String name;
        Map<String, String> parameters;

        HandlerEntry(String name, Map<String, String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        String getName() {
            return name;
        }

        Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            String s = name;
            if (!parameters.isEmpty()) {
                s += parameters;
            }
            return s;
        }
    }
}
