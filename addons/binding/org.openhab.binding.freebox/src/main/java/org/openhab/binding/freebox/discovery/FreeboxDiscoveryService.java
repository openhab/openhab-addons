/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.lan.LanHostConfig;
import org.matmaul.freeboxos.lan.LanHostL3Connectivity;
import org.matmaul.freeboxos.lan.LanHostsConfig;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.config.FreeboxNetInterfaceConfiguration;
import org.openhab.binding.freebox.handler.FreeboxHandler;
import org.openhab.binding.freebox.internal.FreeboxDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxDiscoveryService} is responsible for discovering all things
 * except the Freebox Server thing itself
 *
 * @author Laurent Garnier
 */
public class FreeboxDiscoveryService extends AbstractDiscoveryService implements FreeboxDataListener {

    private static final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    private static final String PHONE_ID = "wired";

    private FreeboxHandler bridgeHandler;

    /**
     * Creates a FreeboxDiscoveryService with background discovery disabled.
     */
    public FreeboxDiscoveryService(FreeboxHandler freeboxBridgeHandler) {
        super(FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.bridgeHandler = freeboxBridgeHandler;
    }

    public void activate() {
        bridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterDataListener(this);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        try {
            LanHostsConfig lanHostsConfiguration = bridgeHandler.getFbClient().getLanManager().getAllLanHostsConfig();
            onDataFetched(bridgeHandler.getThing().getUID(), lanHostsConfiguration);
        } catch (FreeboxException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onDataFetched(ThingUID bridge, LanHostsConfig hostsConfig) {
        if (bridge == null) {
            return;
        }

        // Phone
        ThingUID thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_PHONE, bridge, PHONE_ID);
        if (thingUID != null) {
            logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge)
                    .withLabel("Wired phone").build();
            thingDiscovered(discoveryResult);
        }

        if (hostsConfig != null) {

            // Network devices
            for (LanHostConfig hostConfig : hostsConfig.getConfig()) {
                String mac = hostConfig.getMAC();
                if ((mac != null) && !mac.isEmpty()) {
                    String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
                    thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE, bridge, uid);
                    String name = ((hostConfig.getPrimaryName() == null) || hostConfig.getPrimaryName().isEmpty())
                            ? ("Freebox Network Device " + mac) : hostConfig.getPrimaryName();
                    if (thingUID != null) {
                        logger.trace("Adding new Freebox Network Device {} to inbox", thingUID);
                        Map<String, Object> properties = new HashMap<>(1);
                        if ((hostConfig.getVendorName() != null) && !hostConfig.getVendorName().isEmpty()) {
                            properties.put(Thing.PROPERTY_VENDOR, hostConfig.getVendorName());
                        }
                        properties.put(FreeboxNetDeviceConfiguration.MAC_ADDRESS, mac);
                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withBridge(bridge).withLabel(name).build();
                        thingDiscovered(discoveryResult);
                    }

                    // Network interfaces
                    if (hostConfig.getL3connectivities() != null) {
                        for (LanHostL3Connectivity l3 : hostConfig.getL3connectivities()) {
                            String addr = l3.getAddr();
                            if ((addr != null) && !addr.isEmpty()) {
                                uid = addr.replaceAll("[^A-Za-z0-9_]", "_");
                                thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE,
                                        bridge, uid);
                                name = addr;
                                if ((hostConfig.getPrimaryName() != null) && !hostConfig.getPrimaryName().isEmpty()) {
                                    name += " (" + (hostConfig.getPrimaryName() + ")");
                                }
                                if (thingUID != null) {
                                    logger.trace("Adding new Freebox Network Interface {} to inbox", thingUID);
                                    Map<String, Object> properties = new HashMap<>(1);
                                    if ((hostConfig.getVendorName() != null) && !hostConfig.getVendorName().isEmpty()) {
                                        properties.put(Thing.PROPERTY_VENDOR, hostConfig.getVendorName());
                                    }
                                    properties.put(FreeboxNetInterfaceConfiguration.IP_ADDRESS, addr);
                                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                            .withProperties(properties).withBridge(bridge).withLabel(name).build();
                                    thingDiscovered(discoveryResult);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
