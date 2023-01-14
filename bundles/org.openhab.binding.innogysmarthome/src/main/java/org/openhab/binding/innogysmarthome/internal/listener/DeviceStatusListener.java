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
package org.openhab.binding.innogysmarthome.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;
import org.openhab.binding.innogysmarthome.internal.client.entity.event.Event;

/**
 * The {@link DeviceStatusListener} is called, when {@link Device}s are added, removed or changed.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given {@link Device} has changed.
     *
     * @param device
     *            The device which received the state update.
     */
    public void onDeviceStateChanged(Device device);

    /**
     * This method is called whenever the state of a {@link Device} is changed by the given {@link Event}.
     *
     * @param device
     * @param event
     *
     */
    public void onDeviceStateChanged(Device device, Event event);
}
