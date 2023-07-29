/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity.event;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class BaseEvent {

    public static final String TYPE_STATE_CHANGED = "StateChanged";// "device/SHC.RWE/1.0/event/StateChanged";
    public static final String TYPE_NEW_MESSAGE_RECEIVED = "NewMessageReceived"; // "device/SHC.RWE/1.0/event/NewMessageReceived";
    public static final String TYPE_MESSAGE_CREATED = "MessageCreated";
    public static final String TYPE_MESSAGE_DELETED = "MessageDeleted"; // "device/SHC.RWE/1.0/event/MessageDeleted";
    public static final String TYPE_DISCONNECT = "Disconnect"; // "/event/Disconnect";
    public static final String TYPE_CONFIGURATION_CHANGED = "ConfigurationChanged"; // "device/SHC.RWE/1.0/event/ConfigChanged";
    public static final String TYPE_CONTROLLER_CONNECTIVITY_CHANGED = "/event/ControllerConnectivityChanged"; // "device/SHC.RWE/1.0/event/ControllerConnectivityChanged";
    public static final String TYPE_BUTTON_PRESSED = "ButtonPressed";

    public static final Set<String> SUPPORTED_EVENT_TYPES = Collections
            .unmodifiableSet(Stream.of(TYPE_STATE_CHANGED, TYPE_NEW_MESSAGE_RECEIVED, TYPE_MESSAGE_CREATED,
                    TYPE_MESSAGE_DELETED, TYPE_DISCONNECT, TYPE_CONFIGURATION_CHANGED,
                    TYPE_CONTROLLER_CONNECTIVITY_CHANGED, TYPE_BUTTON_PRESSED).collect(Collectors.toSet()));

    /**
     * The event sequence number – the gateway keeps track and adds a sequence number to each event for the client to
     * identify order and missing events
     */
    private Integer sequenceNumber;

    /**
     * Specifies the type of the event. The type must be the full path to uniquely reference the event definition.
     * Always available.
     */
    private String type;

    /**
     * Date and time when the event occurred in the system. Always available.
     */
    private String timestamp;

    /**
     * @return the sequenceNumber
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param sequenceNumber the sequenceNumber to set
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns true, if the {@link Event} is a ConfigChanged event.
     *
     * @return
     */
    public boolean isConfigChangedEvent() {
        return TYPE_CONFIGURATION_CHANGED.equals(getType());
    }

    /**
     * Returns true, if the {@link Event} is a ControllerConnectivityChanged event.
     *
     * @return
     */
    public boolean isControllerConnectivityChangedEvent() {
        return TYPE_CONTROLLER_CONNECTIVITY_CHANGED.equals(getType());
    }

    /**
     * Returns true, if the {@link Event} is a Disconnect event.
     *
     * @return
     */
    public boolean isDisconnectedEvent() {
        return TYPE_DISCONNECT.equals(getType());
    }

    /**
     * Returns true, if the {@link Event} is a MessageDeletedEvent.
     *
     * @return
     */
    public boolean isMessageDeletedEvent() {
        return TYPE_MESSAGE_DELETED.equals(getType());
    }

    /**
     * Returns true, if the {@link Event} is a NewMessageReceivedEvent.
     *
     * @return
     */
    public boolean isNewMessageReceivedEvent() {
        return TYPE_NEW_MESSAGE_RECEIVED.equals(getType());
    }

    /**
     * Returns true, if the {@link Event} is a StateChangedEvent.
     *
     * @return
     */
    public boolean isStateChangedEvent() {
        return TYPE_STATE_CHANGED.equals(getType());
    }
}
