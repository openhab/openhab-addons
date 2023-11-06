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
package org.openhab.binding.satel.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event listener interface. All classes that want to receive Satel events must
 * implement this interface.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public interface SatelEventListener {

    /**
     * Event handler for connection status events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(ConnectionStatusEvent event) {
    }

    /**
     * Event handler for state events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(IntegraStateEvent event) {
    }

    /**
     * Event handler for status events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(IntegraStatusEvent event) {
    }

    /**
     * Event handler for Integra version events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(IntegraVersionEvent event) {
    }

    /**
     * Event handler for communication module version events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(ModuleVersionEvent event) {
    }

    /**
     * Event handler for events with list of new states.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(NewStatesEvent event) {
    }

    /**
     * Event handler for zone temperature events.
     *
     * @param event incoming event to handle
     */
    default void incomingEvent(ZoneTemperatureEvent event) {
    }
}
