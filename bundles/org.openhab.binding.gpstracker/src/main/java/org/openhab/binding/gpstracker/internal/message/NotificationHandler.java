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
package org.openhab.binding.gpstracker.internal.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpstracker.internal.message.dto.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.dto.TransitionMessage;

/**
 * Handler for notification messages between trackers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class NotificationHandler {
    /**
     * Location notifications need to be sent to the own tracker. Only the last location is saved for each tracker
     * in the group.
     */
    private Map<String, LocationMessage> locationNotifications = new HashMap<>();

    /**
     * TransitionMessage notification to send out to the own tracker. Notifications are saved in order they were
     * received.
     */
    private Map<String, List<TransitionMessage>> transitionNotifications = new HashMap<>();

    /**
     * Handling notification sent by other trackers.
     *
     * @param msg Notification message.
     */
    public void handleNotification(LocationMessage msg) {
        synchronized (this) {
            String trackerId = msg.getTrackerId();
            if (msg instanceof TransitionMessage) {
                List<TransitionMessage> transitionMessages = transitionNotifications.computeIfAbsent(trackerId,
                        k -> new ArrayList<>());
                if (transitionMessages != null) {
                    transitionMessages.add((TransitionMessage) msg);
                }
            } else {
                locationNotifications.put(trackerId, msg);
            }
        }
    }

    /**
     * Collect all notifications about friend trackers.
     *
     * @return List of notification messages from friend trackers need to sent out
     */
    public List<LocationMessage> getNotifications() {
        List<LocationMessage> ret;
        synchronized (this) {
            ret = new ArrayList<>(locationNotifications.values());
            transitionNotifications.values().forEach(ret::addAll);
            locationNotifications.clear();
            transitionNotifications.clear();
        }
        return ret;
    }
}
