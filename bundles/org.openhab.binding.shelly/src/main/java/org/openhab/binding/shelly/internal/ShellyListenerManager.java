/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.shelly.internal.handler.ShellyDeviceListener;

/**
 * {@link ShellyListenerManager} manages a list of deviceListeners
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyListenerManager {
    private final Set<ShellyDeviceListener> deviceListeners = new CopyOnWriteArraySet<>();

    /**
     * Registers a listener, which is informed about device details.
     *
     * @param listener the listener to register
     */
    public synchronized void register(ShellyDeviceListener listener) {
        if (!deviceListeners.contains(listener)) {
            deviceListeners.add(listener);
        }
    }

    /**
     * Unregisters a given listener.
     *
     * @param listener the listener to unregister
     */
    public synchronized void unregister(ShellyDeviceListener listener) {
        if (deviceListeners.contains(listener)) {
            deviceListeners.remove(listener);
        }
    }

    public Set<ShellyDeviceListener> getList() {
        return deviceListeners;
    }

}
