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
package org.openhab.binding.ipcamera.internal;

import static org.openhab.binding.ipcamera.internal.IpCameraBindingConstants.*;

import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ipcamera.internal.onvif.OnvifDiscovery;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpCameraDiscoveryService} is responsible for auto finding cameras that have Onvif
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "binding.ipcamera")
public class IpCameraDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(IpCameraDiscoveryService.class);
    private final NetworkAddressService networkAddressService;

    @Activate
    public IpCameraDiscoveryService(@Reference NetworkAddressService networkAddressService) {
        super(SUPPORTED_THING_TYPES, 0, false);
        this.networkAddressService = networkAddressService;
    }

    @Override
    protected void startBackgroundDiscovery() {
    }

    @Override
    protected void deactivate() {
        super.deactivate();
    }

    public void newCameraFound(String brand, String hostname, int onvifPort) {
        ThingTypeUID thingtypeuid = new ThingTypeUID("ipcamera", brand);
        ThingUID thingUID = new ThingUID(thingtypeuid, hostname.replace(".", ""));
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withProperty(CONFIG_IPADDRESS, hostname).withProperty(CONFIG_ONVIF_PORT, onvifPort)
                .withLabel(brand + " camera @" + hostname).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        OnvifDiscovery onvifDiscovery = new OnvifDiscovery(networkAddressService, this);
        try {
            onvifDiscovery.discoverCameras();
        } catch (UnknownHostException | InterruptedException e) {
            logger.warn(
                    "IpCamera Discovery has an issue discovering the network settings to find cameras with. Try setting up the camera manually.");
        }
        stopScan();
    }
}
