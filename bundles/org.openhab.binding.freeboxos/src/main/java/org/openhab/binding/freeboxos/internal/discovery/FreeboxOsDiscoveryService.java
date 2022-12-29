/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.MissingPermissionException;
import org.openhab.binding.freeboxos.internal.api.home.HomeManager;
import org.openhab.binding.freeboxos.internal.api.home.HomeNode;
import org.openhab.binding.freeboxos.internal.api.lan.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.api.player.Player;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemConf;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.api.wifi.APManager;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.binding.freeboxos.internal.handler.FreeboxOsHandler;
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
    private static final int DISCOVERY_TIME_SECONDS = 10;
    private static final int BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private final Logger logger = LoggerFactory.getLogger(FreeboxOsDiscoveryService.class);

    private Optional<ScheduledFuture<?>> backgroundFuture = Optional.empty();
    private @Nullable FreeboxOsHandler bridgeHandler;

    public FreeboxOsDiscoveryService() {
        super(THINGS_TYPES_UIDS, DISCOVERY_TIME_SECONDS);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof FreeboxOsHandler) {
            bridgeHandler = (FreeboxOsHandler) handler;
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
        backgroundFuture = Optional.of(scheduler.scheduleWithFixedDelay(this::startScan,
                BACKGROUND_SCAN_REFRESH_MINUTES, BACKGROUND_SCAN_REFRESH_MINUTES, TimeUnit.MINUTES));
    }

    @Override
    protected void stopBackgroundDiscovery() {
        backgroundFuture.ifPresent(future -> future.cancel(true));
        backgroundFuture = Optional.empty();
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Freebox discovery scan");
        FreeboxOsHandler localHandler = bridgeHandler;
        if (localHandler != null && localHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                ThingUID bridgeUID = localHandler.getThing().getUID();
                Map<String, LanHost> lanHosts = localHandler.getManager(LanBrowserManager.class).getHostsMap();
                discoverServer(localHandler, bridgeUID);
                discoverPhone(localHandler, bridgeUID);
                discoverRepeater(localHandler, bridgeUID, lanHosts);
                discoverPlayer(localHandler, bridgeUID, lanHosts);
                discoverVM(localHandler, bridgeUID, lanHosts);
                discoverHome(localHandler, bridgeUID);
                if (localHandler.getConfiguration().discoverNetDevice) {
                    discoverHosts(localHandler, bridgeUID, lanHosts);
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery : {}", e.getMessage());
            }
        }
    }

    private void discoverHome(FreeboxOsHandler localHandler, ThingUID bridgeUID) throws FreeboxException {
        try {
            HomeManager homeManager = localHandler.getManager(HomeManager.class);
            List<HomeNode> homeNodes = homeManager.getHomeNodes();
            homeNodes.forEach(node -> {
                DiscoveryResultBuilder discoveryResultBuilder = NodeConfigurationBuilder.getInstance()
                        .configure(bridgeUID, node);
                if (discoveryResultBuilder != null) {
                    DiscoveryResult discoveryResult = discoveryResultBuilder.withLabel(node.getLabel())
                            .withBridge(bridgeUID).build();
                    thingDiscovered(discoveryResult);
                }
            });
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Home {}", e.getPermission());
        }
    }

    private void discoverPhone(FreeboxOsHandler localHandler, ThingUID bridgeUID) throws FreeboxException {
        try {
            PhoneManager phoneManager = localHandler.getManager(PhoneManager.class);
            List<PhoneStatus> statuses = phoneManager.getPhoneStatuses();
            statuses.forEach(config -> {
                ThingUID thingUID = new ThingUID(THING_TYPE_LANDLINE, bridgeUID, Long.toString(config.getId()));
                logger.debug("Adding new Freebox Phone {} to inbox", thingUID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withProperty(ClientConfiguration.ID, config.getId()).withLabel(config.getType().name())
                        .build();
                thingDiscovered(discoveryResult);
            });
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Phone {}", e.getPermission());
        }
    }

    private void discoverVM(FreeboxOsHandler localHandler, ThingUID bridgeUID, Map<String, LanHost> lanHosts)
            throws FreeboxException {
        try {
            VmManager vmManager = localHandler.getManager(VmManager.class);
            vmManager.getDevices().forEach(vm -> {
                String mac = vm.getMac();
                lanHosts.remove(mac);

                ThingUID thingUID = new ThingUID(THING_TYPE_VM, bridgeUID, macToUid(mac));
                logger.debug("Adding new VM Device {} to inbox", thingUID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                        .withLabel(String.format("%s (VM)", vm.getName()))
                        .withProperty(ClientConfiguration.ID, vm.getId()).withProperty(Thing.PROPERTY_MAC_ADDRESS, mac)
                        .build();
                thingDiscovered(discoveryResult);
            });
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover VM {}", e.getPermission());
        }
    }

    private void discoverHosts(FreeboxOsHandler localHandler, ThingUID bridgeUID, Map<String, LanHost> lanHosts)
            throws FreeboxException {
        try {
            Map<String, AccessPointHost> apHosts = localHandler.getManager(APManager.class).getHostsMap();
            Map<String, @Nullable LanHost> repHosts = localHandler.getManager(RepeaterManager.class).getHostsMap();
            List<String> wifiMacs = Stream.concat(apHosts.keySet().stream(), repHosts.keySet().stream())
                    .collect(Collectors.toList());

            lanHosts.entrySet().forEach(entry -> {
                LanHost lanHost = entry.getValue();
                if (lanHost.isReachable()) {
                    String mac = entry.getKey();

                    ThingUID thingUID = new ThingUID(wifiMacs.contains(mac) ? THING_TYPE_WIFI_HOST : THING_TYPE_HOST,
                            bridgeUID, macToUid(mac));
                    logger.debug("Adding new Freebox Network Host {} to inbox", thingUID);
                    DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac).withTTL(300)
                            .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(
                                    lanHost.getPrimaryName().orElse(String.format("Freebox Network Device %s", mac)));
                    thingDiscovered(builder.build());
                }
            });
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Hosts {}", e.getPermission());
        }
    }

    private void discoverRepeater(FreeboxOsHandler localHandler, ThingUID bridgeUID, Map<String, LanHost> lanHosts)
            throws FreeboxException {
        try {
            List<Repeater> repeaters = localHandler.getManager(RepeaterManager.class).getDevices();
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
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Repeater {}", e.getPermission());
        }
    }

    private void discoverServer(FreeboxOsHandler localHandler, ThingUID bridgeUID) throws FreeboxException {
        try {
            SystemConf config = localHandler.getManager(SystemManager.class).getConfig();

            ThingTypeUID targetType = config.getBoardName().startsWith("fbxgw7") ? THING_TYPE_DELTA
                    : THING_TYPE_REVOLUTION;
            ThingUID thingUID = new ThingUID(targetType, bridgeUID, config.getSerial());
            logger.debug("Adding new Freebox Server {} to inbox", thingUID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, config.getMac())
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                    .withLabel(config.getPrettyName().orElse("Freebox Server")).build();
            thingDiscovered(discoveryResult);
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Server {}", e.getPermission());
        }
    }

    private void discoverPlayer(FreeboxOsHandler localHandler, ThingUID bridgeUID, Map<String, LanHost> lanHosts)
            throws FreeboxException {
        try {
            PlayerManager playMgr = localHandler.getManager(PlayerManager.class);
            for (Player player : playMgr.getDevices()) {
                lanHosts.remove(player.getMac());
                ThingUID thingUID = new ThingUID(player.isApiAvailable() ? THING_TYPE_ACTIVE_PLAYER : THING_TYPE_PLAYER,
                        bridgeUID, Integer.toString(player.getId()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel(player.getName()).withProperty(Thing.PROPERTY_MAC_ADDRESS, player.getMac())
                        .withProperty(ClientConfiguration.ID, player.getId())
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                thingDiscovered(discoveryResult);
            }
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Player {}", e.getPermission());
        }
    }

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
    }
}
