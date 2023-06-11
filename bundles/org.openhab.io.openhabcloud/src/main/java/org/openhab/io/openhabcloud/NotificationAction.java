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
package org.openhab.io.openhabcloud;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.model.script.engine.action.ActionDoc;
import org.openhab.io.openhabcloud.internal.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides static methods that can be used in automation rules
 * for sending notifications to the native apps.
 *
 * @author Victor Belov - Initial contribution
 * @author Kai Kreuzer - migrated code to ESH APIs
 */
@NonNullByDefault
public class NotificationAction {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAction.class);

    public static @Nullable CloudService cloudService;

    /**
     * Sends a simple push notification to mobile devices of user
     *
     * @param userId the cloud user id of the recipient
     * @param message the body of the notification
     *
     */
    @ActionDoc(text = "Sends a push notification to mobile devices of user with userId")
    public static void sendNotification(String userId, String message) {
        sendNotification(userId, message, null, null);
    }

    /**
     * Sends an advanced push notification to mobile devices of user
     *
     * @param userId the cloud user id of the recipient
     * @param message the body of the notification
     * @param icon name for the notification
     * @param severity category for the notification
     *
     */
    @ActionDoc(text = "Sends a push notification to mobile devices of user with userId")
    public static void sendNotification(String userId, String message, @Nullable String icon,
            @Nullable String severity) {
        logger.debug("sending notification '{}' to user {}", message, userId);
        if (cloudService != null) {
            cloudService.sendNotification(userId, message, icon, severity);
        }
    }

    /**
     * Sends a simple notification to log. Log notifications are not pushed to user
     * devices but are shown to all account users in notifications log.
     *
     * @param message the body of the notification
     *
     */
    @ActionDoc(text = "Sends a log notification which is shown in notifications log to all account users")
    public static void sendLogNotification(String message) {
        sendLogNotification(message, null, null);
    }

    /**
     * Sends an advanced notification to log. Log notifications are not pushed to user
     * devices but are shown to all account users in notifications log.
     *
     * @param message the body of the notification
     * @param icon name for the notification
     * @param severity category for the notification
     *
     */
    @ActionDoc(text = "Sends a log notification which is shown in notifications log to all account users")
    public static void sendLogNotification(String message, @Nullable String icon, @Nullable String severity) {
        logger.debug("sending log notification '{}'", message);
        if (cloudService != null) {
            cloudService.sendLogNotification(message, icon, severity);
        }
    }

    /**
     * Sends a simple broadcast notification. Broadcast notifications are pushed to all
     * mobile devices of all users of the account
     *
     * @param message the body of the notification
     *
     */
    @ActionDoc(text = "Sends a broadcast notification to all mobile devices of all account users")
    public static void sendBroadcastNotification(String message) {
        sendBroadcastNotification(message, null, null);
    }

    /**
     * Sends an advanced broadcast notification. Broadcast notifications are pushed to all
     * mobile devices of all users of the account
     *
     * @param message the body of the notification
     * @param icon name for the notification
     * @param severity category for the notification
     *
     */
    @ActionDoc(text = "Sends a push notification to mobile devices of user with userId")
    public static void sendBroadcastNotification(String message, @Nullable String icon, @Nullable String severity) {
        logger.debug("sending broadcast notification '{}' to all users", message);
        if (cloudService != null) {
            cloudService.sendBroadcastNotification(message, icon, severity);
        }
    }
}
