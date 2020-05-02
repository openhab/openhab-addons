/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.account;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smarther.internal.api.model.Notification;

/**
 * Interface to decouple Smarther Notification Handler implementation from other code.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public interface SmartherNotificationHandler extends ThingHandler {

    /**
     * Tells whether the handler supports notifications.
     *
     * @return true if the handler supports notifications; false otherwise
     */
    boolean useNotifications();

    /**
     * Registers a new notification endpoint to BTicino/Legrand C2C notification service.
     *
     * @param plantId Id of the location the endpoint belongs to
     */
    void registerNotification(String plantId);

    /**
     * Handles a new notifications arrived from the BTicino/Legrand C2C notification service.
     *
     * @param notification Then received notification
     */
    void handleNotification(Notification notification);

    /**
     * Unregisters a notification endpoint previously registered to BTicino/Legrand C2C notification service.
     *
     * @param plantId Id of the location the endpoint belongs to
     */
    void unregisterNotification(String plantId);

}
