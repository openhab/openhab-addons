/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.freebox.internal.FreeboxBindingConstants;
import org.openhab.binding.freebox.internal.FreeboxDataListener;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHostL3Connectivity;
import org.openhab.binding.freebox.internal.config.FreeboxAirPlayDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetDeviceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxNetInterfaceConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxServerConfiguration;
import org.openhab.binding.freebox.internal.handler.FreeboxHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxDiscoveryService} is responsible for discovering all things
 * except the Freebox Server thing itself
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - add discovery settings
 * @author Laurent Garnier - use new internal API manager
 */
public class FreeboxDiscoveryService extends AbstractDiscoveryService implements FreeboxDataListener {

    private final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    private static final String PHONE_ID = "wired";

    private FreeboxHandler bridgeHandler;
    private boolean discoverPhone;
    private boolean discoverNetDevice;
    private boolean discoverNetInterface;
    private boolean discoverAirPlayReceiver;

    /**
     * Creates a FreeboxDiscoveryService with background discovery disabled.
     */
    public FreeboxDiscoveryService(FreeboxHandler freeboxBridgeHandler) {
        super(FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.bridgeHandler = freeboxBridgeHandler;
        this.discoverPhone = true;
        this.discoverNetDevice = true;
        this.discoverNetInterface = true;
        this.discoverAirPlayReceiver = true;
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
        applyConfig(configProperties);
        bridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterDataListener(this);
        super.deactivate();
    }

    @Override
    public void applyConfig(Map<String, Object> configProperties) {
        if (configProperties != null) {
            Object property = configProperties.get(FreeboxServerConfiguration.DISCOVER_PHONE);
            if (property != null) {
                discoverPhone = ((Boolean) property).booleanValue();
            }
            property = configProperties.get(FreeboxServerConfiguration.DISCOVER_NET_DEVICE);
            if (property != null) {
                discoverNetDevice = ((Boolean) property).booleanValue();
            }
            property = configProperties.get(FreeboxServerConfiguration.DISCOVER_NET_INTERFACE);
            if (property != null) {
                discoverNetInterface = ((Boolean) property).booleanValue();
            }
            property = configProperties.get(FreeboxServerConfiguration.DISCOVER_AIRPLAY_RECEIVER);
            if (property != null) {
                discoverAirPlayReceiver = ((Boolean) property).booleanValue();
            }
        }
        logger.debug("Freebox discovery - discoverPhone : {}", discoverPhone);
        logger.debug("Freebox discovery - discoverNetDevice : {}", discoverNetDevice);
        logger.debug("Freebox discovery - discoverNetInterface : {}", discoverNetInterface);
        logger.debug("Freebox discovery - discoverAirPlayReceiver : {}", discoverAirPlayReceiver);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                List<FreeboxLanHost> lanHosts = bridgeHandler.getApiManager().getLanHosts();
                List<FreeboxAirMediaReceiver> airPlayDevices = bridgeHandler.getApiManager().getAirMediaReceivers();
                onDataFetched(bridgeHandler.getThing().getUID(), lanHosts, airPlayDevices);
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery", e);
            }
        }
    }

    @Override
    public void onDataFetched(ThingUID bridge, List<FreeboxLanHost> lanHosts,
            List<FreeboxAirMediaReceiver> airPlayDevices) {
        if (bridge == null) {
            return;
        }

        ThingUID thingUID;
        DiscoveryResult discoveryResult;

        if (discoverPhone) {
            // Phone
            thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_PHONE, bridge, PHONE_ID);
            logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
            discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridge).withLabel("Wired phone")
                    .build();
            thingDiscovered(discoveryResult);
        }

        if (lanHosts != null && (discoverNetDevice || discoverNetInterface)) {
            // Network devices
            for (FreeboxLanHost host : lanHosts) {
                String mac = host.getMAC();
                if (StringUtils.isNotEmpty(mac)) {
                    if (discoverNetDevice) {
                        String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
                        thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE, bridge, uid);
                        String name = StringUtils.isEmpty(host.getPrimaryName()) ? ("Freebox Network Device " + mac)
                                : host.getPrimaryName();
                        logger.trace("Adding new Freebox Network Device {} to inbox", thingUID);
                        Map<String, Object> properties = new HashMap<>(1);
                        if (StringUtils.isNotEmpty(host.getVendorName())) {
                            properties.put(Thing.PROPERTY_VENDOR, host.getVendorName());
                        }
                        properties.put(FreeboxNetDeviceConfiguration.MAC_ADDRESS, mac);
                        discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                                .withBridge(bridge).withLabel(name).build();
                        thingDiscovered(discoveryResult);
                    }

                    // Network interfaces
                    if (host.getL3Connectivities() != null && discoverNetInterface) {
                        for (FreeboxLanHostL3Connectivity l3 : host.getL3Connectivities()) {
                            String addr = l3.getAddr();
                            if (StringUtils.isNotEmpty(addr)) {
                                String uid = addr.replaceAll("[^A-Za-z0-9_]", "_");
                                thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE,
                                        bridge, uid);
                                String name = addr;
                                if (StringUtils.isNotEmpty(host.getPrimaryName())) {
                                    name += " (" + (host.getPrimaryName() + ")");
                                }
                                logger.trace("Adding new Freebox Network Interface {} to inbox", thingUID);
                                Map<String, Object> properties = new HashMap<>(1);
                                if (StringUtils.isNotEmpty(host.getVendorName())) {
                                    properties.put(Thing.PROPERTY_VENDOR, host.getVendorName());
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

        if (airPlayDevices != null && discoverAirPlayReceiver) {
            // AirPlay devices
            for (FreeboxAirMediaReceiver device : airPlayDevices) {
                String name = device.getName();
                boolean videoCapable = device.isVideoCapable();
                logger.debug("AirPlay Device name {} video capable {}", name, videoCapable);
                // The Freebox API allows pushing media only to receivers with photo or video capabilities
                // but not to receivers with only audio capability; so receivers without video capability
                // are ignored by the discovery
                if (StringUtils.isNotEmpty(name) && videoCapable) {
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
