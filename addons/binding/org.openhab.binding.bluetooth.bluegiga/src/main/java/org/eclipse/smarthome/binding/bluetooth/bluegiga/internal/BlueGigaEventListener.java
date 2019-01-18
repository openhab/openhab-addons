/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth.bluegiga.internal;

/**
 * Listener interface for events
 *
 * @author Chris Jackson - Initial contribution and API
 *
 */
public interface BlueGigaEventListener {
    /**
     * Called when an event is received
     *
     * @param event the {@link BlueGigaResponse} just received
     */
    void bluegigaEventReceived(BlueGigaResponse event);
}
