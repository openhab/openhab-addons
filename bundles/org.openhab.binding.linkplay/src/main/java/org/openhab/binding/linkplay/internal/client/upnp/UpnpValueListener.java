/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Listener interface for receiving UPnP value updates from LinkPlay devices.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface UpnpValueListener {

    /**
     * Called when a UPnP value is received from the device.
     *
     * @param variable the variable name that was updated
     * @param value the value that was received (may be null)
     * @param service the UPnP service that sent the update
     */
    void onUpnpValueReceived(@Nullable String variable, @Nullable String value, String service);
}
