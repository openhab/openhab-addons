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
 * This event is triggered when a device's 'TxPower' property is changed, typically due to receiving an advertisement
 * packet from the device.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class TXPowerEvent extends BlueZEvent {

    private short txPower;

    public TXPowerEvent(String dbusPath, short txpower) {
        super(dbusPath);
        this.txPower = txpower;
    }

    public short getTxPower() {
        return this.txPower;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onTxPowerUpdate(this);
    }
}
