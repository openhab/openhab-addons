/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.test;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.openhab.binding.milight.MilightBindingConstants;

/**
 * A simple discovery service that will make the emulated V6 bridge visible to the Inbox of OH.
 * Enable this in OSGI-INF/TestDiscovery.xml with enabled="true".
 *
 * @author David Graeff
 * @since 2.1
 */
public class TestDiscovery extends AbstractDiscoveryService {
    @SuppressWarnings("unused")
    private EmulatedV6Bridge server;

    public TestDiscovery() {
        super(MilightBindingConstants.BRIDGE_THING_TYPES_UIDS, 2, true);
        try {
            server = new EmulatedV6Bridge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startScan() {
    }
}