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
package org.openhab.binding.milight.internal.test;

import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple discovery service that will make the emulated V6 bridge visible to the Inbox of OH.
 * Enable this in OSGI-INF/TestDiscovery.xml with enabled="true".
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = DiscoveryService.class, enabled = false)
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
