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
package org.openhab.binding.dirigera.internal.discovery;

import static org.openhab.binding.dirigera.internal.Constants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * {@link DirigeraDiscoveryService} notifies about about devices found by
 * DIRIGERA hub
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class,
        DirigeraDiscoveryService.class }, configurationPid = "dirigera.device.discovery")
public class DirigeraDiscoveryService extends AbstractDiscoveryService {

    @Activate
    public DirigeraDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 90);
    }

    public void deviceDiscovered(DiscoveryResult result) {
        thingDiscovered(result);
    }

    public void deviceRemoved(DiscoveryResult result) {
        thingRemoved(result.getThingUID());
    }

    @Override
    protected void startScan() {
        // no manual scan supported
    }
}
