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

import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiController;
import org.openhab.binding.unifi.internal.api.model.UniFiUnknownClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWirelessClient;

import com.google.gson.InstanceCreator;

/**
 * The {@link UniFiClientInstanceCreator} creates instances of {@link UniFiClient}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClientInstanceCreator implements InstanceCreator<UniFiClient> {

    private final UniFiController controller;

    public UniFiClientInstanceCreator(UniFiController controller) {
        this.controller = controller;
    }

    @Override
    public UniFiClient createInstance(Type type) {
        if (UniFiUnknownClient.class.equals(type)) {
            return new UniFiUnknownClient(controller);
        }
        if (UniFiWirelessClient.class.equals(type)) {
            return new UniFiWirelessClient(controller);
        }
        if (UniFiWiredClient.class.equals(type)) {
            return new UniFiWiredClient(controller);
        }
        return null;
    }
}
