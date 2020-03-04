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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.AirMediaReceiversResponse;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.VirtualMachine;
import org.openhab.binding.freebox.internal.api.model.VirtualMachinesResponse;
import org.openhab.binding.freebox.internal.config.AirPlayerConfiguration;
import org.openhab.binding.freebox.internal.config.FreeboxAPIConfiguration;
import org.openhab.binding.freebox.internal.config.VirtualMachineConfiguration;
import org.openhab.binding.freebox.internal.handler.FreeboxAPIHandler;
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

    private final Logger logger = LoggerFactory.getLogger(FreeboxDiscoveryService.class);
    // The call to listDevices is fast
    private static final int DISCOVERY_TIME_SECONDS = 10;
    // Check every minute for new devices
    private static final long BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private static final String PHONE_ID = "wired";

    private @NonNullByDefault({}) FreeboxAPIHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private @NonNullByDefault({}) ThingUID bridgeUID;
    private @NonNullByDefault({}) FreeboxAPIConfiguration configuration;

    /**
     * Creates a FreeboxDiscoveryService
     *
     * @param bindingConfig
     */
    public FreeboxDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    // Je ne comprends pas pourquoi ceci n'est jamais appelé
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
        if (handler instanceof FreeboxAPIHandler) {
            bridgeHandler = (FreeboxAPIHandler) handler;
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
            try {
                if (configuration.discoverPhone) {
                    discoverPhone();
                }
                if (configuration.discoverVM) {
                    discoverVM();
                }
                if (configuration.discoverAirPlayReceiver) {
                    discoverAirPlay();
                }
                if (configuration.discoverNetDevice) {
                    discoverNetDevices();
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery", e);
            }
        }
    }

    private void discoverNetDevices() throws FreeboxException {
        List<VirtualMachine> vms = bridgeHandler.executeGet(VirtualMachinesResponse.class, null);
        List<LanHost> lanHosts = bridgeHandler.getLanHosts();

        List<LanHost> foundMacs = lanHosts.stream()
                .filter(host -> vms.stream().anyMatch(vm -> host.sameMac(vm.getMac()))).collect(Collectors.toList());

        List<LanHost> foundPlayer = lanHosts.stream().filter(host -> "freebox_player".equals(host.getHostType()))
                .collect(Collectors.toList());

        lanHosts.stream().filter(i -> !foundPlayer.contains(i)).filter(i -> !foundMacs.contains(i)).forEach(host -> {
            String mac = host.getMAC();
            String uid = mac.replaceAll("[^A-Za-z0-9_]", "_");
            ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_NET_DEVICE, bridgeUID, uid);
            logger.trace("Adding new Freebox Network Device {} to inbox", thingUID);
            DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withBridge(bridgeUID)
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withProperty("interface", host.getInterface())
                    .withLabel(host.hasPrimaryName() ? host.getPrimaryName()
                            : String.format("Freebox Network Device %s", mac));
            if (host.hasVendorName()) {
                builder = builder.withProperty(Thing.PROPERTY_VENDOR, host.getVendorName());
            }
            thingDiscovered(builder.build());

        });

    }

    private void discoverAirPlay() throws FreeboxException {
        List<AirMediaReceiver> airPlayDevices = bridgeHandler.executeGet(AirMediaReceiversResponse.class,
                null);
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
        List<VirtualMachine> vms = bridgeHandler.executeGet(VirtualMachinesResponse.class, null);
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

    private void discoverPhone() {
        ThingUID thingUID = new ThingUID(FREEBOX_THING_TYPE_PHONE, bridgeUID, PHONE_ID);
        logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withLabel("Wired phone").build();
        thingDiscovered(discoveryResult);
    }

}
