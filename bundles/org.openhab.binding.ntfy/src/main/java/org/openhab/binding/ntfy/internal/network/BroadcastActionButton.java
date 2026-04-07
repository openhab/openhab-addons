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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Action button that represents a broadcast action attached to a notification.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class BroadcastActionButton extends ActionButtonBase {

    private @Nullable String params;

    /**
     * Creates a broadcast action button descriptor.
     *
     * @param label the label shown for the action
     * @param clearNotification whether executing the action should clear the notification
     * @param params additional parameters passed with the broadcast action (may be null)
     */
    public BroadcastActionButton(String label, Boolean clearNotification, @Nullable String params) {
        super(label, clearNotification);
        this.params = params;
    }

    @Override
    public String getHeader() {
        return "broadcast, " + label + (params != null ? ", " + params : "")
                + (clearNotification ? ", clear=true" : "");
    }
}
