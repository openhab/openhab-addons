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
 * @author Dan Cunningham - Extended notification enhancements
 */
@NonNullByDefault
public class NotificationAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationAction.class);

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
     * @param tag for the notification (formerly severity)
     *
     */
    @ActionDoc(text = "Sends a push notification to mobile devices of user with userId")
    public static void sendNotification(String userId, String message, @Nullable String icon, @Nullable String tag) {
        sendNotification(userId, message, icon, tag, null, null, null, null, null, null, null);
    }

    /**
     * Sends an advanced push notification to mobile devices of user
     *
     * @param userId the cloud user id of the recipient
     * @param message the body of the notification
     * @param icon name for the notification
     * @param tag for the notification
     * @param title for the notification
     * @param referenceId an identifier used to collapse and hide notifications
     * @param onClickAction the action to perform when clicked
     * @param mediaAttachmentUrl the media to attach to a notification
     * @param actionButton1 an action button in the format "Title=Action"
     * @param actionButton2 an action button in the format "Title=Action"
     * @param actionButton3 an action button in the format "Title=Action"
     *
     */
    @ActionDoc(text = "Sends a push notification to mobile devices of user with userId")
    public static void sendNotification(String userId, String message, @Nullable String icon, @Nullable String tag,
            @Nullable String title, @Nullable String referenceId, @Nullable String onClickAction,
            @Nullable String mediaAttachmentUrl, @Nullable String actionButton1, @Nullable String actionButton2,
            @Nullable String actionButton3) {
        LOGGER.debug("sending notification '{}' to user {}", message, userId);
        if (cloudService != null) {
            cloudService.sendNotification(userId, message, icon, tag, title, referenceId, onClickAction,
                    mediaAttachmentUrl, actionButton1, actionButton2, actionButton3);
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
     * @param tag for the notification (formerly severity)
     *
     */
    @ActionDoc(text = "Sends a log notification which is shown in notifications log to all account users")
    public static void sendLogNotification(String message, @Nullable String icon, @Nullable String tag) {
        LOGGER.debug("sending log notification '{}'", message);
        if (cloudService != null) {
            cloudService.sendLogNotification(message, icon, tag);
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
     * @param tag for the notification (formerly severity)
     *
     */
    @ActionDoc(text = "Sends a broadcast notification to all mobile devices of all account users")
    public static void sendBroadcastNotification(String message, @Nullable String icon, @Nullable String tag) {
        sendBroadcastNotification(message, icon, tag, null, null, null, null, null, null, null);
    }

    /**
     * Sends an advanced broadcast notification. Broadcast notifications are pushed to all
     * mobile devices of all users of the account
     *
     * @param message the body of the notification
     * @param icon name for the notification
     * @param tag for the notification
     * @param title for the notification
     * @param referenceId an identifier used to collapse and hide notifications
     * @param onClickAction the action to perform when clicked
     * @param mediaAttachmentUrl the media to attach to a notification
     * @param actionButton1 an action button in the format "Title=Action"
     * @param actionButton2 an action button in the format "Title=Action"
     * @param actionButton3 an action button in the format "Title=Action"
     *
     */
    @ActionDoc(text = "Sends a broadcast notification to all mobile devices of all account users")
    public static void sendBroadcastNotification(String message, @Nullable String icon, @Nullable String tag,
            @Nullable String title, @Nullable String referenceId, @Nullable String onClickAction,
            @Nullable String mediaAttachmentUrl, @Nullable String actionButton1, @Nullable String actionButton2,
            @Nullable String actionButton3) {
        LOGGER.debug("sending broadcast notification '{}' to all users", message);
        if (cloudService != null) {
            cloudService.sendBroadcastNotification(message, icon, tag, title, referenceId, onClickAction,
                    mediaAttachmentUrl, actionButton1, actionButton2, actionButton3);
        }
    }

    /**
     * Hides notifications that contains a matching reference id to all mobile devices of a single user.
     *
     * @param userId the cloud user id of the recipient
     * @param referenceId the user reference id
     *
     */
    @ActionDoc(text = "Hides notifications that contain the reference id on mobile devices of user with userId")
    public static void hideNotificationByReferenceId(String userId, String referenceId) {
        if (cloudService != null) {
            cloudService.hideNotificationByReferenceId(userId, referenceId);
        }
    }

    /**
     * Hides notifications that contains a matching reference id to all mobile devices of all users of the account
     *
     * @param referenceId the user reference id
     *
     */
    @ActionDoc(text = "Hides notifications that contain the reference id on all mobile devices of all account users")
    public static void hideBroadcastNotificationByReferenceId(String referenceId) {
        if (cloudService != null) {
            cloudService.hideBroadcastNotificationByReferenceId(referenceId);
        }
    }

    /**
     * Hides notifications that are associated with a tag to all mobile devices of a single user.
     *
     * @param userId the cloud user id of the recipient
     * @param tag the tag associated with notifications
     *
     */
    @ActionDoc(text = "Hides notifications that are associated with a tag on mobile devices of user with userId")
    public static void hideNotificationByTag(String userId, String tag) {
        if (cloudService != null) {
            cloudService.hideNotificationByTag(userId, tag);
        }
    }

    /**
     * Hides notifications that are associated with a tag to all mobile devices of all users of the account
     *
     * @param tag the tag associated with notifications
     *
     */
    @ActionDoc(text = "Hides notifications that are associated with a tag on all mobile devices of all account users")
    public static void hideBroadcastNotificationByTag(String tag) {
        if (cloudService != null) {
            cloudService.hideBroadcastNotificationByTag(tag);
        }
    }
}
