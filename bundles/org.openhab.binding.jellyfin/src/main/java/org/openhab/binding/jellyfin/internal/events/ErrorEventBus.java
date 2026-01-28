/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.events;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event bus for error events using the Observer pattern
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ErrorEventBus {

    private final CopyOnWriteArrayList<ErrorEventListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Add an error event listener
     * 
     * @param listener The listener to add
     */
    public void addListener(ErrorEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove an error event listener
     * 
     * @param listener The listener to remove
     */
    public void removeListener(ErrorEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Publish an error event to all listeners
     * 
     * @param event The error event to publish
     */
    public void publishEvent(ErrorEvent event) {
        for (ErrorEventListener listener : listeners) {
            try {
                listener.onErrorEvent(event);
            } catch (Exception e) {
                // Log but don't let listener exceptions break the event bus
                System.err.println("Error in event listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
