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

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiversResponse;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.LanHostsResponse;
import org.openhab.binding.freebox.internal.api.model.LanInterface;
import org.openhab.binding.freebox.internal.api.model.LanInterfacesResponse;
import org.openhab.binding.freebox.internal.api.model.VirtualMachine;
import org.openhab.binding.freebox.internal.api.model.VirtualMachinesResponse;
import org.openhab.binding.freebox.internal.config.AirPlayerConfiguration;
import org.openhab.binding.freebox.internal.config.ServerConfiguration;
import org.openhab.binding.freebox.internal.config.VirtualMachineConfiguration;
import org.openhab.binding.freebox.internal.handler.ServerHandler;
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
@NonNullByDefault
public class FreeboxDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(FREEBOX_THING_TYPE_PHONE, FREEBOX_THING_TYPE_HOST, FREEBOX_THING_TYPE_AIRPLAY, FREEBOX_THING_TYPE_VM)
            .collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);
    // The call to listDevices is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;
    // Check every minute for new devices
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private static final String PHONE_ID = "wired";

    private @NonNullByDefault({}) ServerHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private @NonNullByDefault({}) ThingUID bridgeUID;
    private @NonNullByDefault({}) ServerConfiguration configuration;

    public FreeboxDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        if (configuration != null) {
            Map<String, @Nullable Object> properties = new HashMap<>();
            properties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, configuration.background);
            super.activate(properties);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ServerHandler) {
            bridgeHandler = (ServerHandler) handler;
            bridgeUID = bridgeHandler.getThing().getUID();
            configuration = bridgeHandler.getConfiguration();
            activate(null);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        backgroundFuture = scheduler.scheduleWithFixedDelay(this::startScan, BACKGROUND_SCAN_REFRESH_MINUTES,
                BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (backgroundFuture != null) {
            backgroundFuture.cancel(true);
            backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            if (configuration.discoverPhone) {
                discoverPhone();
            }
            try {
                if (configuration.discoverVM) {
                    discoverVM();
                }
                if (configuration.discoverAirPlayReceiver) {
                    discoverAirPlay();
                }
                if (configuration.discoverNetDevice) {
                    discoverHosts();
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery : {}", e.getMessage());
            }
        }
    }

    private void discoverPhone() {
        ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_PHONE, bridgeUID, PHONE_ID);
        logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withLabel("Wired phone").build();
        thingDiscovered(discoveryResult);
    }

    private void discoverHosts() throws FreeboxException {
        // List<VirtualMachine> vms = bridgeHandler.getApiManager().executeGet(VirtualMachinesResponse.class, null);
        List<LanHost> lanHosts = getLanHosts();

        /*
         * List<LanHost> foundMacs = lanHosts.stream()
         * .filter(host -> vms.stream().anyMatch(vm -> host.sameMac(vm.getMac()))).collect(Collectors.toList());
         *
         * List<LanHost> foundPlayer = lanHosts.stream().filter(host -> "freebox_player".equals(host.getHostType()))
         * .collect(Collectors.toList());
         */
        lanHosts.stream()
                ./* filter(i -> !foundPlayer.contains(i)).filter(i -> !foundMacs.contains(i)). */forEach(host -> {
                    String mac = host.getMAC();
                    String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
                    ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_HOST, bridgeUID, uid);
                    logger.trace("Adding new Freebox Network Host {} to inbox", thingUID);
                    DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID)
                            .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withBridge(bridgeUID)
                            .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac)
                            .withLabel(host.hasPrimaryName() ? host.getPrimaryName()
                                    : String.format("Freebox Network Device %s", mac));
                    if (host.hasVendorName()) {
                        builder = builder.withProperty(Thing.PROPERTY_VENDOR, host.getVendorName());
                    }
                    thingDiscovered(builder.build());
                });

    }

    private void discoverAirPlay() throws FreeboxException {
        List<AirMediaReceiver> airPlayDevices = bridgeHandler.getApiManager()
                .executeGet(AirMediaReceiversResponse.class, null);
        // The Freebox API allows pushing media only to receivers with photo or video capabilities but not to receivers
        // with only audio capability; so receivers without video capability are ignored by the discovery
        airPlayDevices.stream().filter(AirMediaReceiver::hasName).filter(AirMediaReceiver::isVideoCapable)
                .forEach(device -> {
                    String name = device.getName();
                    String uid = name.replaceAll("[^A-Za-z0-9_]", "_");
                    ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_AIRPLAY, bridgeUID, uid);
                    logger.trace("Adding new Freebox AirPlay Device {} to inbox", thingUID);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withRepresentationProperty(AirPlayerConfiguration.NAME)
                            .withProperty(AirPlayerConfiguration.NAME, name).withBridge(bridgeUID)
                            .withLabel(name + " (AirPlay)").build();
                    thingDiscovered(discoveryResult);
                });
    }

    private void discoverVM() throws FreeboxException {
        List<VirtualMachine> vms = bridgeHandler.getApiManager().executeGet(VirtualMachinesResponse.class, null);
        vms.forEach(vm -> {
            String uid = vm.getMac().replaceAll("[^A-Za-z0-9_]", "_");
            ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_VM, bridgeUID, uid);
            logger.trace("Adding new VM Device {} to inbox", thingUID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(vm.getName() + " (VM)").withProperty(Thing.PROPERTY_MAC_ADDRESS, vm.getMac())
                    .withProperty(VirtualMachineConfiguration.VM_ID, vm.getId())
                    .withRepresentationProperty(VirtualMachineConfiguration.VM_ID).build();
            thingDiscovered(discoveryResult);
        });
    }

    private List<LanHost> getLanHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();
        List<LanInterface> lans = bridgeHandler.getApiManager().executeGet(LanInterfacesResponse.class, null);
        lans.stream().filter(LanInterface::hasHosts).forEach(lan -> {
            try {
                List<LanHost> lanHosts = bridgeHandler.getApiManager().executeGet(LanHostsResponse.class,
                        lan.getName());
                hosts.addAll(lanHosts);
            } catch (FreeboxException e) {
                logger.warn("Error getting hosts for interface {}", lan.getName());
            }
        });
        return hosts;
    }

}
