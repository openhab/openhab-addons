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
package org.openhab.binding.bluetooth.bluegiga.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener interface for events
 *
 * @author Chris Jackson - Initial contribution and API
 *
 */
@NonNullByDefault
public interface BlueGigaEventListener {
    /**
     * Called when an event is received
     *
     * @param event the {@link BlueGigaResponse} just received
     */
    void bluegigaEventReceived(BlueGigaResponse event);
}
