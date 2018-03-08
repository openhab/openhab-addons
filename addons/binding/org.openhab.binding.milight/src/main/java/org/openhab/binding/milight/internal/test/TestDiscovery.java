/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.test;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.openhab.binding.milight.MilightBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple discovery service that will make the emulated V6 bridge visible to the Inbox of OH.
 * Enable this in OSGI-INF/TestDiscovery.xml with enabled="true".
 *
 * @author David Graeff - Initial contribution
 * @since 2.1
 */
@Component(service = DiscoveryService.class, immediate = true, enabled = false)
public class TestDiscovery extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(TestDiscovery.class);

    @SuppressWarnings("unused")
    private EmulatedV6Bridge server;

    public TestDiscovery() {
        super(MilightBindingConstants.BRIDGE_THING_TYPES_UIDS, 2, true);
        try {
            server = new EmulatedV6Bridge();
        } catch (Exception e) {
            logger.warn("An error occurred", e);
        }
    }

    @Override
    protected void startScan() {
    }
}
