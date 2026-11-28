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
package org.openhab.binding.atv.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener interface for generic device updates.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface DeviceListener {

    /**
     * Informs that the device was unexpectedly disconnected.
     *
     * @param exception error causing the disconnect
     */
    void connectionLost(Exception exception);

    /**
     * Informs that the device connection was (intentionally) closed.
     */
    void connectionClosed();
}
