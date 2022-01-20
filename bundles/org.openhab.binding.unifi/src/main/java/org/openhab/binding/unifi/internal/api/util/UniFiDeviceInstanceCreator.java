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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;

import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;

import com.google.gson.InstanceCreator;

/**
 *
 * The {@link UniFiDeviceInstanceCreator} creates instances of {@link UniFiDevice}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiDeviceInstanceCreator implements InstanceCreator<UniFiDevice> {

    private final UniFiController controller;

    public UniFiDeviceInstanceCreator(UniFiController controller) {
        this.controller = controller;
    }

    @Override
    public UniFiDevice createInstance(Type type) {
        return new UniFiDevice(controller);
    }
}
