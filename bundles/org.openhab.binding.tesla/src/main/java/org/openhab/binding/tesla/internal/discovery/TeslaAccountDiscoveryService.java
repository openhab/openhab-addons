/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.discovery;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.openhab.binding.tesla.internal.TeslaHandlerFactory;
import org.openhab.binding.tesla.internal.command.TeslaCommandExtension;
import org.osgi.service.component.annotations.Component;

/**
 * This is a discovery service, is used by the {@link TeslaCommandExtension} for
 * automatically creating Tesla accounts.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component(service = { TeslaAccountDiscoveryService.class, DiscoveryService.class })
public class TeslaAccountDiscoveryService extends AbstractDiscoveryService {

    public TeslaAccountDiscoveryService() throws IllegalArgumentException {
        super(TeslaHandlerFactory.SUPPORTED_THING_TYPES_UIDS, 10, true);
    }

    @Override
    protected void startScan() {
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }
}
