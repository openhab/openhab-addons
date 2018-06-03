/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.matmaul.freeboxos.FreeboxException;
import org.matmaul.freeboxos.airmedia.AirMediaReceiver;
import org.matmaul.freeboxos.lan.LanHostConfig;
import org.matmaul.freeboxos.lan.LanHostL3Connectivity;
import org.matmaul.freeboxos.lan.LanHostsConfig;
import org.openhab.binding.freebox.FreeboxBindingConstants;
import org.openhab.binding.freebox.FreeboxDataListener;
import org.openhab.binding.freebox.handler.FreeboxHandler;
import org.openhab.binding.freebox.internal.config.FreeboxAirPlayDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetInterfaceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxDiscoveryService} is responsible for discovering all things
 * except the Freebox Server thing itself
 *
 * @author Laurent Garnier - Initial contribution
 */
public class FreeboxDiscoveryService extends AbstractDiscoveryService implements FreeboxDataListener {

    private final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);

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

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
        bridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterDataListener(this);
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                LanHostsConfig lanHostsConfiguration = bridgeHandler.getFbClient().getLanManager()
                        .getAllLanHostsConfig();
                List<AirMediaReceiver> airPlayDevices = bridgeHandler.getFbClient().getAirMediaManager().getReceivers();
                onDataFetched(bridgeHandler.getThing().getUID(), lanHostsConfiguration, airPlayDevices);
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery", e);
            }
        }
    }

    @Override
    public void onDataFetched(ThingUID bridge, LanHostsConfig hostsConfig, List<AirMediaReceiver> airPlayDevices) {
        if (bridge == null) {
            return;
        }

        // Phone
        ThingUID thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_PHONE, bridge, PHONE_ID);
        logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge)
                .withLabel("Wired phone").build();
        thingDiscovered(discoveryResult);

        if (hostsConfig != null) {
            // Network devices
            for (LanHostConfig hostConfig : hostsConfig.getConfig()) {
                String mac = hostConfig.getMAC();
                if (StringUtils.isNotEmpty(mac)) {
                    String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
                    thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE, bridge, uid);
                    String name = StringUtils.isEmpty(hostConfig.getPrimaryName()) ? ("Freebox Network Device " + mac)
                            : hostConfig.getPrimaryName();
                    logger.trace("Adding new Freebox Network Device {} to inbox", thingUID);
                    Map<String, Object> properties = new HashMap<>(1);
                    if (StringUtils.isNotEmpty(hostConfig.getVendorName())) {
                        properties.put(Thing.PROPERTY_VENDOR, hostConfig.getVendorName());
                    }
                    properties.put(FreeboxNetDeviceConfiguration.MAC_ADDRESS, mac);
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridge).withLabel(name).build();
                    thingDiscovered(discoveryResult);

                    // Network interfaces
                    if (hostConfig.getL3connectivities() != null) {
                        for (LanHostL3Connectivity l3 : hostConfig.getL3connectivities()) {
                            String addr = l3.getAddr();
                            if (StringUtils.isNotEmpty(addr)) {
                                uid = addr.replaceAll("[^A-Za-z0-9_]", "_");
                                thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE,
                                        bridge, uid);
                                name = addr;
                                if (StringUtils.isNotEmpty(hostConfig.getPrimaryName())) {
                                    name += " (" + (hostConfig.getPrimaryName() + ")");
                                }
                                logger.trace("Adding new Freebox Network Interface {} to inbox", thingUID);
                                properties = new HashMap<>(1);
                                if (StringUtils.isNotEmpty(hostConfig.getVendorName())) {
                                    properties.put(Thing.PROPERTY_VENDOR, hostConfig.getVendorName());
                                }
                                properties.put(FreeboxNetInterfaceConfiguration.IP_ADDRESS, addr);
                                discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                                        .withBridge(bridge).withLabel(name).build();
                                thingDiscovered(discoveryResult);
                            }
                        }
                    }
                }
            }
        }

        if (airPlayDevices != null) {
            // AirPlay devices
            for (AirMediaReceiver device : airPlayDevices) {
                String name = device.getName();
                Boolean videoCapable = device.isVideoCapable();
                logger.debug("AirPlay Device name {} video capable {}", name, videoCapable);
                // The Freebox API allows pushing media only to receivers with photo or video capabilities
                // but not to receivers with only audio capability; so receivers without video capability
                // are ignored by the discovery
                if (StringUtils.isNotEmpty(name) && Boolean.TRUE.equals(videoCapable)) {
                    String uid = name.replaceAll("[^A-Za-z0-9_]", "_");
                    thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_AIRPLAY, bridge, uid);
                    logger.trace("Adding new Freebox AirPlay Device {} to inbox", thingUID);
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put(FreeboxAirPlayDeviceConfiguration.NAME, name);
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridge).withLabel(name + " (AirPlay)").build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

}
