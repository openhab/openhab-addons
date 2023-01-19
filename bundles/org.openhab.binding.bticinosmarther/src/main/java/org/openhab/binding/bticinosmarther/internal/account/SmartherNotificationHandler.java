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
package org.openhab.binding.bticinosmarther.internal.account;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bticinosmarther.internal.api.dto.Notification;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@code SmartherNotificationHandler} interface is used to decouple the Smarther notification handler
 * implementation from other Bridge code.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public interface SmartherNotificationHandler extends ThingHandler {

    /**
     * Tells whether the Smarther Bridge associated with this handler supports notifications.
     *
     * @return {@code true} if the Bridge supports notifications, {@code false} otherwise
     */
    boolean useNotifications();

    /**
     * Calls the Smarther API to register a new notification endpoint to the C2C Webhook service.
     *
     * @param plantId
     *            the identifier of the plant the notification endpoint belongs to
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the Smarther API
     */
    void registerNotification(String plantId) throws SmartherGatewayException;

    /**
     * Handles a new notifications received from the C2C Webhook notification service.
     *
     * @param notification
     *            the received notification
     */
    void handleNotification(Notification notification);

    /**
     * Calls the Smarther API to unregister a notification endpoint already registered to the C2C Webhook service.
     *
     * @param plantId
     *            the identifier of the plant the notification endpoint belongs to
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the Smarther API
     */
    void unregisterNotification(String plantId) throws SmartherGatewayException;
}
