/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.jupnp.model.ValidationException;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.types.DeviceType;
import org.jupnp.model.types.UDN;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.discovery.YamahaDiscoveryParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a discovery service that returns a connection to the emulated AVR receiver.
 *
 * @author David Graeff - Initial contribution
 */
public class TestDiscoveryService extends AbstractDiscoveryService {
    // The server variable is never used, but the server object is actually used in the background.
    private URL url;
    private Logger logger = LoggerFactory.getLogger(TestDiscoveryService.class);

    public TestDiscoveryService(EmulatedYamahaReceiver server) {
        super(YamahaReceiverBindingConstants.BRIDGE_THING_TYPES_UIDS, 2, true);
        try {
            url = new URL("http", "127.0.0.1", server.getPort(), "");
        } catch (MalformedURLException e) {
            logger.warn("Malformed url", e);
        }
    }

    @Override
    protected void startScan() {
        try {
            DeviceDetails details = new DeviceDetails(url, "Yamaha AVR Test", new ManufacturerDetails("YAMAHA"),
                    new ModelDetails("AVR Test"), "123456789", "1234-1234-1234-1234", null, null, null, null);
            DeviceType type = new DeviceType("org.test", YamahaReceiverBindingConstants.UPNP_TYPE);
            RemoteDeviceIdentity identity = new RemoteDeviceIdentity(
                    new UDN(UUID.fromString("e49825eb-8d1f-4e7a-8de4-c091850597f5")), 10, url, null,
                    InetAddress.getByName(url.getHost()));
            RemoteDevice device = new RemoteDevice(identity, type, details, (RemoteService) null);

            YamahaDiscoveryParticipant testClass = new YamahaDiscoveryParticipant();
            DiscoveryResult discoveryResult = testClass.createResult(device);
            thingDiscovered(discoveryResult);
        } catch (IOException | ValidationException e) {
            logger.warn("Test discovery device creation failed", e);
        }
    }
}
