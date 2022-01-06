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
 * This event is triggered when a device's GATT services get resovled/unresolved.
 * Services become resolved after connecting to a device and become unresolved
 * either due to error or connection issues.
 *
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class ServicesResolvedEvent extends BlueZEvent {

    private boolean resolved;

    public ServicesResolvedEvent(String dbusPath, boolean resolved) {
        super(dbusPath);
        this.resolved = resolved;
    }

    public boolean isResolved() {
        return resolved;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onServicesResolved(this);
    }
}
