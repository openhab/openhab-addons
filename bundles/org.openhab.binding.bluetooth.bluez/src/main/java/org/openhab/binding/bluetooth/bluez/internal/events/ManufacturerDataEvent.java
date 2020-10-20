/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class ManufacturerDataEvent extends BlueZEvent {

    private Map<Short, byte[]> data;

    public ManufacturerDataEvent(String dbusPath, Map<Short, byte[]> data) {
        super(dbusPath, EventType.MANUFACTURER_DATA);
        this.data = data;
    }

    public Map<Short, byte[]> getData() {
        return data;
    }
}
