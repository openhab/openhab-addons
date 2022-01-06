/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This event is triggered when a bluetooth device's 'Connected' property changes.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class ConnectedEvent extends BlueZEvent {

    private boolean connected;

    public ConnectedEvent(String dbusPath, boolean connected) {
        super(dbusPath);
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onConnectedStatusUpdate(this);
    }
}
