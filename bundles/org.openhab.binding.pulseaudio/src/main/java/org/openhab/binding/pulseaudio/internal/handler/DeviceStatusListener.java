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
package org.openhab.binding.pulseaudio.internal.handler;

import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link DeviceStatusListener} is notified when a device status has changed
 * or a device has been removed or added.
 *
 * @author Tobias Bräutigam - Initial contribution
 *
 */
public interface DeviceStatusListener {

    /**
     * This method is called whenever the state of the given device has changed.
     *
     * @param bridge The Pulseaudio bridge the changed device is connected to.
     * @param device The device which received the state update.
     */
    public void onDeviceStateChanged(ThingUID bridge, AbstractAudioDeviceConfig device);

    /**
     * This method us called whenever a device is removed.
     *
     * @param bridge The Pulseaudio bridge the removed device was connected to.
     * @param device The device which is removed.
     */
    public void onDeviceRemoved(PulseaudioBridgeHandler bridge, AbstractAudioDeviceConfig device);

    /**
     * This method us called whenever a device is added.
     *
     * @param bridge The Pulseaudio bridge the added device was connected to.
     * @param device The device which is added.
     */
    public void onDeviceAdded(Bridge bridge, AbstractAudioDeviceConfig device);
}
