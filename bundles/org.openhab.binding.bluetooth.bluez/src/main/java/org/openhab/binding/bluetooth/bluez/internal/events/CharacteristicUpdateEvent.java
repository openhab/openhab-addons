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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This event is triggered when a update notification is received for a characteristic.
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class CharacteristicUpdateEvent extends BlueZEvent {

    private byte[] data;

    public CharacteristicUpdateEvent(String dbusPath, byte[] data) {
        super(dbusPath);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onCharacteristicNotify(this);
    }
}
