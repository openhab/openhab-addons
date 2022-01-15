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
import org.openhab.binding.unifi.internal.api.model.UniFiSite;

import com.google.gson.InstanceCreator;

/**
 *
 * The {@link UniFiSiteInstanceCreator} creates instances of {@link UniFiSite}s during the JSON unmarshalling of
 * controller responses.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiSiteInstanceCreator implements InstanceCreator<UniFiSite> {

    private final UniFiController controller;

    public UniFiSiteInstanceCreator(UniFiController controller) {
        this.controller = controller;
    }

    @Override
    public UniFiSite createInstance(Type type) {
        return new UniFiSite(controller);
    }
}
