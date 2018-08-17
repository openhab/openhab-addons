/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for notification messages between trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class NotificationHandler {
    /**
     * Location notifications need to be sent to the own tracker. Only the last location is saved for each tracker
     * in the group.
     */
    private Map<String, Location> locationNotifications = new HashMap<>();

    /**
     * Transition notification to send out to the own tracker. Notifications are saved in order they were received.
     */
    private Map<String, List<Transition>> transitionNotifications = new HashMap<>();

    /**
     * Handling notification sent by other trackers.
     *
     * @param msg Notification message.
     */
    void handleNotification(AbstractBaseMessage msg) {
        synchronized (this) {
            String trackerId = msg.getTrackerId();
            if (msg instanceof Location) {
                locationNotifications.put(trackerId, (Location) msg);
            } else {
                List<Transition> transitions = transitionNotifications
                        .computeIfAbsent(trackerId, k -> new ArrayList<>());
                transitions.add((Transition) msg);
            }
        }
    }

    /**
     * Collect all notifications about friend trackers.
     *
     * @return List of notification messages from friend trackers need to sent out
     */
    public List<AbstractBaseMessage> getNotifications() {
        List<AbstractBaseMessage> ret;
        synchronized (this) {
            ret = new ArrayList<>(locationNotifications.values());
            transitionNotifications.values().forEach(ret::addAll);
            locationNotifications.clear();
            transitionNotifications.clear();
        }
        return ret;
    }
}
