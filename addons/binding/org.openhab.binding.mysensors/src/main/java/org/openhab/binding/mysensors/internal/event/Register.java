/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;
import java.util.List;

/**
 * Generic interface for an EventListener register
 *
 * @author Andrea Cioni
 *
 * @param <T> the EventListener to register
 */
public interface Register<T extends EventListener> {

    /**
     * Check if a given listener is already registered
     *
     * @param listener to be checked
     *
     * @return true if listener is already registered
     */
    public boolean isEventListenerRegisterd(T listener);

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public void addEventListener(T listener);

    /**
     * Remove a listener
     *
     * @param listener the one to be removed
     */
    public void removeEventListener(T listener);

    /**
     * Remove all listeners
     */
    public void clearAllListeners();

    /**
     * Get a list of all the registerd listener
     *
     * @return
     */
    public List<T> getEventListeners();
}
