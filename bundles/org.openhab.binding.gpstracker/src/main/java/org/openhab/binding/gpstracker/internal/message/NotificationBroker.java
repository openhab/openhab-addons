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
package org.openhab.binding.gpstracker.internal.message;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification broker.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class NotificationBroker {
    /**
     * Handlers
     */
    private Map<String, NotificationHandler> handlers = new HashMap<>();

    /**
     * Register new handler.
     *
     * @param trackerId Tracker id
     * @param handler Notification handler
     */
    public void registerHandler(String trackerId, NotificationHandler handler) {
        handlers.put(trackerId, handler);
    }

    public void sendNotification(LocationMessage msg) {
        String trackerId = msg.getTrackerId();
        handlers.entrySet().stream().filter(e -> !e.getKey().equals(trackerId))
                .forEach(e -> e.getValue().handleNotification(msg));
    }
}
