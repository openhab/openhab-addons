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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.DeviceFeature;

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
public class FeatureTemplate {
    private String type;
    private @Nullable HandlerEntry dispatcher;
    private @Nullable HandlerEntry pollHandler;
    private @Nullable HandlerEntry defaultMsgHandler;
    private @Nullable HandlerEntry defaultCmdHandler;
    private List<HandlerEntry> messageHandlers = new ArrayList<>();
    private List<HandlerEntry> commandHandlers = new ArrayList<>();
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
     * @param msgHandler the message handler entry to add
     */
    public void addMessageHandler(HandlerEntry msgHandler) {
        messageHandlers.add(msgHandler);
    }

    /**
     * Adds a command handler to this feature template
     *
     * @param cmdHandler the command handler entry to add
     */
    public void addCommandHandler(HandlerEntry cmdHandler) {
        commandHandlers.add(cmdHandler);
    }

    /**
     * Returns a newly created DeviceFeature instance
     *
     * @param name the feature name
     * @param device the feature device
     * @return the feature which this template describes
     */
    public DeviceFeature build(String name, Device device) {
        DeviceFeature feature = new DeviceFeature(name, type, device);
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

        for (HandlerEntry msgHandler : messageHandlers) {
            MessageHandler handler = MessageHandler.makeHandler(msgHandler.getName(), msgHandler.getParameters(),
                    feature);
            if (handler != null) {
                feature.addMessageHandler(handler.getId(), handler);
            }
        }

        for (HandlerEntry cmdHandler : commandHandlers) {
            CommandHandler handler = CommandHandler.makeHandler(cmdHandler.getName(), cmdHandler.getParameters(),
                    feature);
            if (handler != null) {
                feature.addCommandHandler(handler.getId(), handler);
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
            s += "|msgHandlers:"
                    + messageHandlers.stream().map(HandlerEntry::toString).collect(Collectors.joining(","));
        }
        if (!commandHandlers.isEmpty()) {
            s += "|cmdHandlers:"
                    + commandHandlers.stream().map(HandlerEntry::toString).collect(Collectors.joining(","));
        }
        return s;
    }
}
