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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This event is triggered when an update to a device's service data is received.
 *
 * @author Peter Rosenberg - Initial Contribution
 *
 */
@NonNullByDefault
public class ServiceDataEvent extends BlueZEvent {

    private final Map<String, byte[]> data;

    public ServiceDataEvent(String dbusPath, Map<String, byte[]> data) {
        super(dbusPath);
        this.data = data;
    }

    public Map<String, byte[]> getData() {
        return data;
    }

    @Override
    public void dispatch(BlueZEventListener listener) {
        listener.onServiceDataUpdate(this);
    }
}
