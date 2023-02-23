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
package org.openhab.binding.heos.internal.resources;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.payload.Media;

/**
 * The {@link HeosSystemEventListener } is used for classes which
 * wants to inform players or groups about change events
 * from the HEOS system. Classes which wants to be informed
 * has to implement the {@link HeosEventListener} and register at
 * the class which extends this {@link HeosSystemEventListener}
 *
 * @author Johannes Einig - Initial contribution
 * @author Martin van Wingerden - change handling of stop/pause depending on playing item type
 */
@NonNullByDefault
public class HeosSystemEventListener {
    private final Set<HeosEventListener> listenerList = new CopyOnWriteArraySet<>();

    /**
     * Register a listener from type {@link HeosEventListener} to be notified by
     * a change event
     *
     * @param listener the lister from type {@link HeosEventListener} for change events
     */
    public void addListener(HeosEventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Removes the listener from the notification list
     *
     * @param listener the listener from type {@link HeosEventListener} to be removed
     */
    public void removeListener(HeosEventListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Notifies the registered listener of a changed state type event
     *
     * @param eventObject the command of the event
     */
    public void fireStateEvent(HeosEventObject eventObject) {
        listenerList.forEach(element -> element.playerStateChangeEvent(eventObject));
    }

    /**
     * Notifies the registered listener of a changed media type event
     *
     * @param pid the ID of the player or group which has changed
     * @param media the media information
     */
    public void fireMediaEvent(String pid, Media media) {
        listenerList.forEach(element -> element.playerMediaChangeEvent(pid, media));
    }

    /**
     * Notifies the registered listener if a change of the bridge state
     *
     * @param event the event type
     * @param success the result (success or fail)
     * @param command the command of the event
     */
    public void fireBridgeEvent(String event, boolean success, Object command) {
        for (HeosEventListener heosEventListener : listenerList) {
            heosEventListener.bridgeChangeEvent(event, success, command);
        }
    }
}
