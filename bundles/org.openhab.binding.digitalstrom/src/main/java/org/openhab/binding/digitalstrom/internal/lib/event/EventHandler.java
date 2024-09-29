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
package org.openhab.binding.digitalstrom.internal.lib.event;

import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.event.types.EventItem;

/**
 * The {@link EventHandler} can be implemented to get informed by {@link EventItem}'s through the {@link EventListener}.
 * <br>
 * For that the {@link #getSupportedEvents()} and
 * {@link #supportsEvent(String)} methods have to be implemented, so that
 * the {@link EventListener} knows whitch events it has to subscribe at the digitalSTROM-server and which handler has
 * to be informed. <br>
 * The implementation of the {@link EventHandler} also has to be registered through
 * {@link EventListener#addEventHandler(EventHandler)} to the {@link EventListener} and the {@link EventListener} has to
 * be started.<br>
 * <br>
 * To handle the {@link EventItem} the method {@link #handleEvent(EventItem)} has to be implemented.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public interface EventHandler {

    /**
     * Handles an {@link EventItem} e.g. which was detected by the {@link EventListener}.
     *
     * @param eventItem to handle
     */
    void handleEvent(EventItem eventItem);

    /**
     * Returns a {@link List} that contains the supported events.
     *
     * @return supported events
     */
    List<String> getSupportedEvents();

    /**
     * Returns true, if the {@link EventHandler} supports the given event.
     *
     * @param eventName to check
     * @return true, if event is supported, otherwise false
     */
    boolean supportsEvent(String eventName);

    /**
     * Returns the unique id of the {@link EventHandler}.
     *
     * @return uid of the EventHandler
     */
    String getUID();

    /**
     * Sets an {@link EventListener} to this {@link EventHandler}.
     *
     * @param eventListener to set
     */
    void setEventListener(EventListener eventListener);

    /**
     * Unsets an {@link EventListener} to this {@link EventHandler}.
     *
     * @param eventListener to unset
     */
    void unsetEventListener(EventListener eventListener);
}
