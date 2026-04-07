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
 * Action button that represents a copy action attached to a notification.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class CopyActionButton extends ActionButtonBase {

    private String value;

    /**
     * Creates a copy action button descriptor.
     *
     * @param label the label shown for the action
     * @param clearNotification whether executing the action should clear the notification
     * @param value the value that will be copied when the action is executed
     */
    public CopyActionButton(String label, Boolean clearNotification, String value) {
        super(label, clearNotification);
        this.value = value;
    }

    @Override
    public String getHeader() {
        return "copy, " + label + ", " + value + (clearNotification ? ", clear=true" : "");
    }
}
