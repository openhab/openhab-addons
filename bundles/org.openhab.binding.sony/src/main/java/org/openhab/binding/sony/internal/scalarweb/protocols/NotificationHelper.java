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
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.scalarweb.models.api.Notifications;

/**
 * This helper class provides services to determine if a notification is enabled AFTER the first time. The typical use
 * case is that we want to refresh some data at startup but to allow notifications to refresh the data from that point
 * on (ignoring any refresh calls)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NotificationHelper {
    /** Contains the (readonly) set of notification names that are enabled */
    private final Set<String> notificationNames;

    /** A set of enable notifications that haven't been tried yet */
    private final Set<String> firstTime = ConcurrentHashMap.newKeySet();

    /**
     * Constructs the helper from the notifications
     * 
     * @param notifications a non-null notifications
     */
    public NotificationHelper(final Notifications notifications) {
        Objects.requireNonNull(notifications, "notifications cannot be null");

        final Set<String> names = new HashSet<>();
        notifications.getEnabled().stream().map(e -> e.getName()).forEach(e -> {
            if (e != null && StringUtils.isNotEmpty(e)) {
                names.add(e);
                firstTime.add(e);
            }
        });

        notificationNames = Collections.unmodifiableSet(names);
    }

    /**
     * Determines if the specified notification is enabled (after the first time) or not. Note that the notification
     * also needs to have been enabled for this to return true
     * 
     * @param name a non-null, non-empty notification name
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(final String name) {
        Validate.notEmpty(name, "name cannot be empty");

        return notificationNames.contains(name) && !firstTime.remove(name);
    }
}
