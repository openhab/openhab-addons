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
 * This event is triggered when a device's 'Name' bluez property changes
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class NameEvent extends BlueZEvent {

    private String name;

    public NameEvent(String dbusPath, String name) {
        super(dbusPath);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onNameUpdate(this);
    }
}
