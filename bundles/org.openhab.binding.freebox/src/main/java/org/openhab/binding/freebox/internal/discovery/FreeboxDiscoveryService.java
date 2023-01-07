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
package org.openhab.binding.freebox.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxDiscoveryService} is responsible for discovering all things
 * except the Freebox Server thing itself
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - add discovery settings
 * @author Laurent Garnier - use new internal API manager
 * @author Laurent Garnier - use ThingHandlerService
 */
@NonNullByDefault
public class FreeboxDiscoveryService extends AbstractDiscoveryService
        implements FreeboxDataListener, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);

    private static final int SEARCH_TIME = 10;

    private static final String PHONE_ID = "wired";

    private @Nullable FreeboxHandler bridgeHandler;
    private boolean discoverPhone;
    private boolean discoverNetDevice;
    private boolean discoverNetInterface;
    private boolean discoverAirPlayReceiver;

    /**
     * Creates a FreeboxDiscoveryService with background discovery disabled.
     */
    public FreeboxDiscoveryService() {
        super(FreeboxBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.discoverPhone = true;
        this.discoverNetDevice = true;
        this.discoverNetInterface = true;
        this.discoverAirPlayReceiver = true;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof FreeboxHandler) {
            bridgeHandler = (FreeboxHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
        FreeboxHandler handler = bridgeHandler;
        if (handler != null) {
            Configuration config = handler.getThing().getConfiguration();
            Object property = config.get(FreeboxServerConfiguration.DISCOVER_PHONE);
            discoverPhone = property != null ? ((Boolean) property).booleanValue() : true;
            property = config.get(FreeboxServerConfiguration.DISCOVER_NET_DEVICE);
            discoverNetDevice = property != null ? ((Boolean) property).booleanValue() : true;
            property = config.get(FreeboxServerConfiguration.DISCOVER_NET_INTERFACE);
            discoverNetInterface = property != null ? ((Boolean) property).booleanValue() : true;
            property = config.get(FreeboxServerConfiguration.DISCOVER_AIRPLAY_RECEIVER);
            discoverAirPlayReceiver = property != null ? ((Boolean) property).booleanValue() : true;
            logger.debug("Freebox discovery - discoverPhone : {}", discoverPhone);
            logger.debug("Freebox discovery - discoverNetDevice : {}", discoverNetDevice);
            logger.debug("Freebox discovery - discoverNetInterface : {}", discoverNetInterface);
            logger.debug("Freebox discovery - discoverAirPlayReceiver : {}", discoverAirPlayReceiver);

            handler.registerDataListener(this);
        }
    }

    @Override
    public void deactivate() {
        FreeboxHandler handler = bridgeHandler;
        if (handler != null) {
            handler.unregisterDataListener(this);
        }
        super.deactivate();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        FreeboxHandler handler = bridgeHandler;
        if (handler != null && handler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                List<FreeboxLanHost> lanHosts = handler.getApiManager().getLanHosts();
                List<FreeboxAirMediaReceiver> airPlayDevices = handler.getApiManager().getAirMediaReceivers();
                onDataFetched(handler.getThing().getUID(), lanHosts, airPlayDevices);
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery", e);
            }
        }
    }

    @Override
    public void onDataFetched(ThingUID bridge, @Nullable List<FreeboxLanHost> lanHosts,
            @Nullable List<FreeboxAirMediaReceiver> airPlayDevices) {
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
                String primaryName = host.getPrimaryName();
                String vendorName = host.getVendorName();
                if (mac != null && !mac.isEmpty()) {
                    if (discoverNetDevice) {
                        String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
                        thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE, bridge, uid);
                        String name = (primaryName == null || primaryName.isEmpty()) ? ("Freebox Network Device " + mac)
                                : primaryName;
                        logger.trace("Adding new Freebox Network Device {} to inbox", thingUID);
                        Map<String, Object> properties = new HashMap<>(1);
                        if (vendorName != null && !vendorName.isEmpty()) {
                            properties.put(Thing.PROPERTY_VENDOR, vendorName);
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
                            if (addr != null && !addr.isEmpty()) {
                                String uid = addr.replaceAll("[^A-Za-z0-9_]", "_");
                                thingUID = new ThingUID(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE,
                                        bridge, uid);
                                String name = addr;
                                if (primaryName != null && !primaryName.isEmpty()) {
                                    name += " (" + (primaryName + ")");
                                }
                                logger.trace("Adding new Freebox Network Interface {} to inbox", thingUID);
                                Map<String, Object> properties = new HashMap<>(1);
                                if (vendorName != null && !vendorName.isEmpty()) {
                                    properties.put(Thing.PROPERTY_VENDOR, vendorName);
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
                if (name != null && !name.isEmpty() && videoCapable) {
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
