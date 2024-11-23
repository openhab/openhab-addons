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
 * This is triggered when a bluetooth adapter's 'Powered' property changes
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class AdapterPoweredChangedEvent extends BlueZEvent {

    private boolean powered;

    public AdapterPoweredChangedEvent(String dbusPath, boolean powered) {
        super(dbusPath);
        this.powered = powered;
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onPoweredChange(this);
    }
}
