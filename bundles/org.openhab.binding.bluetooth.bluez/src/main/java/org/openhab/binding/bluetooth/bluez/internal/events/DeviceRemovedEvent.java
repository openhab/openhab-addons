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
package org.openhab.binding.bluetooth.bluez.internal.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is triggered when BlueZ removes a device object (ObjectManager InterfacesRemoved), meaning
 * any connection to the device is gone and its cached GATT state is no longer valid.
 *
 * @author Vlad Kolotov - Initial Contribution
 *
 */
@NonNullByDefault
public class DeviceRemovedEvent extends BlueZEvent {

    public DeviceRemovedEvent(String dbusPath) {
        super(dbusPath);
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onDeviceRemoved(this);
    }
}
