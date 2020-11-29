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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;
import static org.openhab.binding.freeboxos.internal.config.ApiConfiguration.API_VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.lan.NameSource;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.api.player.Player;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.system.DeviceConfig;
import org.openhab.binding.freeboxos.internal.api.system.SystemConf;
import org.openhab.binding.freeboxos.internal.api.vm.VirtualMachine;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;
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
 * The {@link FreeboxOsDiscoveryService} is responsible for discovering all things
 * except the Freebox API thing itself
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeboxOsDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsDiscoveryService.class);
    private static final int DISCOVERY_TIME_SECONDS = 10;
    private static final int BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private @NonNullByDefault({}) ApiHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> backgroundFuture;
    private @NonNullByDefault({}) ThingUID bridgeUID;
    private @NonNullByDefault({}) ApiConfiguration configuration;

    public FreeboxOsDiscoveryService() {
        super(THINGS_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ApiHandler) {
            bridgeHandler = (ApiHandler) handler;
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
        ScheduledFuture<?> future = this.backgroundFuture;
        if (future != null) {
            future.cancel(true);
            this.backgroundFuture = null;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                discoverServer();
                discoverPhone();
                Map<String, @Nullable LanHost> lanHosts = bridgeHandler.getLanManager().getHostsMap();
                discoverRepeater(lanHosts);
                discoverPlayer(lanHosts);
                discoverVM(lanHosts);
                if (configuration.discoverNetDevice) {
                    discoverHosts(lanHosts);
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery : {}", e.getMessage());
            }
        }
    }

    private void discoverServer() throws FreeboxException {
        final String FBX_DELTA_GW = "fbxgw7";

        SystemConf config = bridgeHandler.getSystemManager().getConfig();

        ThingUID thingUID = new ThingUID(
                config.getBoardName().contains(FBX_DELTA_GW) ? THING_TYPE_DELTA : THING_TYPE_REVOLUTION, bridgeUID,
                config.getSerial());
        logger.trace("Adding new Freebox Server {} to inbox", thingUID);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, config.getBoardName());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, config.getMac());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                .withLabel(config.getPrettyName()).build();
        thingDiscovered(discoveryResult);
    }

    private void discoverPhone() throws FreeboxException {
        PhoneStatus config = bridgeHandler.getPhoneManager().getStatus();
        ThingUID thingUID = new ThingUID(THING_TYPE_LANDLINE, bridgeUID, Long.toString(config.getId()));
        logger.trace("Adding new Freebox Phone {} to inbox", thingUID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withLabel(config.getType().name()).build();
        thingDiscovered(discoveryResult);
    }

    private void discoverRepeater(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        List<Repeater> repeaters = bridgeHandler.getRepeaterManager().getRepeaters();
        repeaters.forEach(repeater -> {
            Map<String, Object> properties = new HashMap<>();
            String mac = repeater.getMac();
            LanHost lanhost = lanHosts.remove(mac);
            if (lanhost != null) {
                String vendor = lanhost.getVendorName();
                if (vendor != null) {
                    properties.put(Thing.PROPERTY_VENDOR, vendor);
                }
            }
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, repeater.getSerial());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, repeater.getFirmwareVersion());
            properties.put(Thing.PROPERTY_MAC_ADDRESS, mac);
            properties.put(ClientConfiguration.ID, repeater.getId());
            properties.put(Thing.PROPERTY_MODEL_ID, repeater.getModel());

            ThingUID thingUID = new ThingUID(THING_TYPE_REPEATER, bridgeUID, Integer.toString(repeater.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(String.format("Repeater %s", repeater.getName())).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
            thingDiscovered(discoveryResult);
        });

    }

    private void discoverPlayer(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        // List<AirMediaReceiver> receivers = bridgeHandler.getAirMediaManager().getReceivers();

        PlayerManager playMgr = bridgeHandler.getPlayerManager();
        for (Player player : playMgr.getPlayers()) {
            Map<String, Object> properties = new HashMap<>();
            DeviceConfig config = playMgr.getConfig(player.getId());
            LanHost lanhost = lanHosts.remove(player.getMac());
            if (lanhost != null) {
                String vendor = lanhost.getVendorName();
                if (vendor != null) {
                    properties.put(Thing.PROPERTY_VENDOR, vendor);
                }
                properties.put(NameSource.UPNP.name(), lanhost.getPrimaryNameOrElse("Freebox Player"));
            }

            properties.put(Thing.PROPERTY_MAC_ADDRESS, player.getMac());
            properties.put(ClientConfiguration.ID, player.getId());
            properties.put(Thing.PROPERTY_MODEL_ID, player.getModel());
            properties.put(API_VERSION, player.getApiVersion());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getSerial());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getFirmwareVersion());

            ThingUID thingUID = new ThingUID(THING_TYPE_PLAYER, bridgeUID, Integer.toString(player.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(config.getPrettyName()).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverVM(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        List<VirtualMachine> vms = bridgeHandler.getVmManager().getVms();
        vms.forEach(vm -> {
            String mac = vm.getMac();
            lanHosts.remove(vm.getMac());

            ThingUID thingUID = new ThingUID(THING_TYPE_VM, bridgeUID, macToUid(mac));
            logger.trace("Adding new VM Device {} to inbox", thingUID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                    .withLabel(String.format("%s (VM)", vm.getName())).withProperty(ClientConfiguration.ID, vm.getId())
                    .build();
            thingDiscovered(discoveryResult);
        });
    }

    private void discoverHosts(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        Map<String, @Nullable AccessPointHost> apHosts = bridgeHandler.getWifiManager().getHostsMap();
        Map<String, @Nullable LanHost> repHosts = bridgeHandler.getRepeaterManager().getHostsMap();
        List<String> wifiMacs = new ArrayList<>();
        wifiMacs.addAll(apHosts.keySet());
        wifiMacs.addAll(repHosts.keySet());

        lanHosts.entrySet().forEach(entry -> {
            LanHost lanHost = entry.getValue();
            if (lanHost != null && lanHost.isReachable()) {
                String mac = entry.getKey();
                Map<String, Object> properties = new HashMap<>();

                String vendor = lanHost.getVendorName();
                if (vendor != null) {
                    properties.put(Thing.PROPERTY_VENDOR, vendor);
                }
                properties.put(Thing.PROPERTY_MAC_ADDRESS, mac);

                ThingUID thingUID = new ThingUID(wifiMacs.contains(mac) ? THING_TYPE_WIFI_HOST : THING_TYPE_HOST,
                        bridgeUID, macToUid(mac));
                logger.trace("Adding new Freebox Network Host {} to inbox", thingUID);
                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withProperties(properties).withTTL(300).withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                        .withLabel(lanHost.getPrimaryNameOrElse(String.format("Freebox Network Device %s", mac)));
                thingDiscovered(builder.build());
            }
        });
    }

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
    }
}
