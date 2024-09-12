/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * This event is triggered when bluetooth advertisement packet is picked up from a device.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class RssiEvent extends BlueZEvent {

    private short rssi;

    public RssiEvent(String dbusPath, short rssi) {
        super(dbusPath);
        this.rssi = rssi;
    }

    public short getRssi() {
        return rssi;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onRssiUpdate(this);
    }
}
