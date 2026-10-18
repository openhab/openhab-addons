/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for action buttons used in message headers.
 * <p>
 * Action buttons are used to describe user actions (view, copy, http, broadcast)
 * that are attached to a notification message and are serialized into the
 * X-Actions request header.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public abstract class ActionButtonBase {
    protected String label;
    protected Boolean clearNotification;

    /**
     * Creates a new action button descriptor.
     *
     * @param label the label shown for the action button
     * @param clearNotification whether the action should clear the notification when executed
     */
    public ActionButtonBase(String label, Boolean clearNotification) {
        this.label = label;
        this.clearNotification = clearNotification;
    }

    /**
     * Returns the serialized header representation of the action for inclusion
     * in the X-Actions HTTP header.
     *
     * @return the header string representation of the action
     */
    public abstract String getHeader();
}
