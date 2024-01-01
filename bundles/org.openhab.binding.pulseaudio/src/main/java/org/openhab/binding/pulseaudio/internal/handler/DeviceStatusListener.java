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
package org.openhab.binding.pulseaudio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.core.thing.Thing;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Tobias Bräutigam - Initial contribution
 *
 */
@NonNullByDefault
public interface DeviceStatusListener {
    /**
     * This method us called whenever a device is added.
     *
     * @param bridge The Pulseaudio bridge the added device was connected to.
     * @param device The device which is added.
     */
    void onDeviceAdded(Thing bridge, AbstractAudioDeviceConfig device);
}
