/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.model.message.header.RootDeviceHeader;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDiscoveryService} is a {@link DiscoveryService} implementation, which can find WeMo UPnP devices in
 * the network.
 *
 * @author Hans-Jörg Merk - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.wemo")
public class WemoDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(WemoDiscoveryService.class);

    public WemoDiscoveryService() {
        super(5);
    }

    private @Nullable UpnpService upnpService;

    @Reference
    protected void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    protected void unsetUpnpService(UpnpService upnpService) {
        this.upnpService = null;
    }

    public void activate() {
        logger.debug("Starting WeMo UPnP discovery...");
        startScan();
    }

    @Override
    public void deactivate() {
        logger.debug("Stopping WeMo UPnP discovery...");
        stopScan();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting UPnP RootDevice search...");
        UpnpService localService = upnpService;
        if (localService != null) {
            localService.getControlPoint().search(new RootDeviceHeader());
        } else {
            logger.debug("upnpService not set");
        }
    }

    @Override
    protected synchronized void stopScan() {
        removeOlderResults(getTimestampOfLastScan());
        super.stopScan();
    }
}
