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
 * This is triggered when BlueZ removes an adapter object (ObjectManager InterfacesRemoved), e.g. a
 * USB Bluetooth dongle being unplugged. Any cached adapter proxy and the devices found through it
 * are then stale and must be invalidated.
 *
 * @author Vlad Kolotov - Initial Contribution
 *
 */
@NonNullByDefault
public class AdapterRemovedEvent extends BlueZEvent {

    public AdapterRemovedEvent(String dbusPath) {
        super(dbusPath);
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onAdapterRemoved(this);
    }
}
