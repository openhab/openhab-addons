/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.lan.LanManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.api.player.Player;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemConf;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiManager;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.binding.freeboxos.internal.handler.FreeboxOsBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
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

    private @NonNullByDefault({}) FreeboxOsBridgeHandler bridgeHandler;
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
        if (handler instanceof FreeboxOsBridgeHandler) {
            bridgeHandler = (FreeboxOsBridgeHandler) handler;
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
                Map<String, @Nullable LanHost> lanHosts = bridgeHandler.getManager(LanManager.class).getHostsMap();
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

    private void discoverPhone() throws FreeboxException {
        PhoneManager phoneManager = bridgeHandler.getManager(PhoneManager.class);
        PhoneStatus config = phoneManager.getStatus();
        ThingUID thingUID = new ThingUID(THING_TYPE_LANDLINE, bridgeUID, Long.toString(config.getId()));
        logger.debug("Adding new Freebox Phone {} to inbox", thingUID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withLabel(config.getType().name()).build();
        thingDiscovered(discoveryResult);
    }

    private void discoverVM(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        VmManager vmManager = bridgeHandler.getManager(VmManager.class);
        vmManager.getVms().forEach(vm -> {
            String mac = vm.getMac();
            lanHosts.remove(mac);

            ThingUID thingUID = new ThingUID(THING_TYPE_VM, bridgeUID, macToUid(mac));
            logger.debug("Adding new VM Device {} to inbox", thingUID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                    .withLabel(String.format("%s (VM)", vm.getName())).withProperty(ClientConfiguration.ID, vm.getId())
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).build();
            thingDiscovered(discoveryResult);
        });
    }

    private void discoverHosts(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        Map<String, @Nullable AccessPointHost> apHosts = bridgeHandler.getManager(WifiManager.class).getHostsMap();
        Map<String, @Nullable LanHost> repHosts = bridgeHandler.getManager(RepeaterManager.class).getHostsMap();
        List<String> wifiMacs = new ArrayList<>();
        wifiMacs.addAll(apHosts.keySet());
        wifiMacs.addAll(repHosts.keySet());

        lanHosts.entrySet().forEach(entry -> {
            LanHost lanHost = entry.getValue();
            if (lanHost != null && lanHost.isReachable()) {
                String mac = entry.getKey();

                ThingUID thingUID = new ThingUID(wifiMacs.contains(mac) ? THING_TYPE_WIFI_HOST : THING_TYPE_HOST,
                        bridgeUID, macToUid(mac));
                logger.debug("Adding new Freebox Network Host {} to inbox", thingUID);
                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withTTL(300)
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                        .withLabel(lanHost.getPrimaryName().orElse(String.format("Freebox Network Device %s", mac)));
                thingDiscovered(builder.build());
            }
        });
    }

    private void discoverRepeater(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        List<Repeater> repeaters = bridgeHandler.getManager(RepeaterManager.class).getRepeaters();
        repeaters.forEach(repeater -> {
            String mac = repeater.getMac();
            lanHosts.remove(mac);

            ThingUID thingUID = new ThingUID(THING_TYPE_REPEATER, bridgeUID, Integer.toString(repeater.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(String.format("Repeater %s", repeater.getName()))
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac)
                    .withProperty(ClientConfiguration.ID, repeater.getId())
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
            thingDiscovered(discoveryResult);
        });
    }

    private void discoverServer() throws FreeboxException {
        SystemConf config = bridgeHandler.getManager(SystemManager.class).getConfig();

        ThingTypeUID targetType = config.getBoardName().startsWith("fbxgw7") ? THING_TYPE_DELTA : THING_TYPE_REVOLUTION;
        ThingUID thingUID = new ThingUID(targetType, bridgeUID, config.getSerial());
        logger.debug("Adding new Freebox Server {} to inbox", thingUID);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperty(Thing.PROPERTY_MAC_ADDRESS, config.getMac())
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                .withLabel(config.getPrettyName().orElse("Freebox Server")).build();
        thingDiscovered(discoveryResult);
    }

    private void discoverPlayer(Map<String, @Nullable LanHost> lanHosts) throws FreeboxException {
        PlayerManager playMgr = bridgeHandler.getManager(PlayerManager.class);
        for (Player player : playMgr.getPlayers()) {
            lanHosts.remove(player.getMac());
            ThingUID thingUID = new ThingUID(player.isApiAvailable() ? THING_TYPE_ACTIVE_PLAYER : THING_TYPE_PLAYER,
                    bridgeUID, Integer.toString(player.getId()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel(player.getName()).withProperty(Thing.PROPERTY_MAC_ADDRESS, player.getMac())
                    .withProperty(ClientConfiguration.ID, player.getId())
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
            thingDiscovered(discoveryResult);
        }
    }

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
    }
}
