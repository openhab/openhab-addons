/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.upnp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jupnp.model.meta.RemoteDevice;

/**
 * A listener for UPnP device events.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface LinkPlayUpnpDeviceListener {
    /**
     * Called when an UPnP RemoteDevice is updated.
     * 
     * @param device the device to update the configuration for
     */
    void updateDeviceConfig(RemoteDevice device);
}
