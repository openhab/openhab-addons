/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        handlers.entrySet().stream().filter(e->!e.getKey().equals(trackerId))
                .forEach(e->e.getValue().handleNotification(msg));
    }
}
