/**
<<<<<<< Upstream, based on origin/main
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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.PermissionException;
import org.openhab.binding.freeboxos.internal.api.rest.APManager;
import org.openhab.binding.freeboxos.internal.api.rest.APManager.Station;
import org.openhab.binding.freeboxos.internal.api.rest.FreeplugManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Status;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.rest.PlayerManager.Player;
import org.openhab.binding.freeboxos.internal.api.rest.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.rest.RepeaterManager.Repeater;
import org.openhab.binding.freeboxos.internal.api.rest.SystemManager;
import org.openhab.binding.freeboxos.internal.api.rest.SystemManager.Config;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager;
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

import inet.ipaddr.mac.MACAddress;

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
        super(Stream.of(THINGS_TYPES_UIDS, HOME_TYPES_UIDS).flatMap(Set::stream).collect(Collectors.toSet()),
                DISCOVERY_TIME_SECONDS);
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

                List<LanHost> lanHosts = localHandler.getManager(LanBrowserManager.class).getHosts().stream()
                        .filter(LanHost::reachable).collect(Collectors.toList());

                discoverServer(localHandler.getManager(SystemManager.class), bridgeUID);
                discoverPhone(localHandler.getManager(PhoneManager.class), bridgeUID);
                discoverPlugs(localHandler.getManager(FreeplugManager.class), bridgeUID);
                discoverRepeater(localHandler.getManager(RepeaterManager.class), bridgeUID, lanHosts);
                discoverPlayer(localHandler.getManager(PlayerManager.class), bridgeUID, lanHosts);
                discoverVM(localHandler.getManager(VmManager.class), bridgeUID, lanHosts);
                discoverHome(localHandler.getManager(HomeManager.class), bridgeUID);
                if (localHandler.getConfiguration().discoverNetDevice) {
                    discoverHosts(localHandler, bridgeUID, lanHosts);
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery : {}", e.getMessage());
            }
        }
    }

    private void discoverHome(HomeManager homeManager, ThingUID bridgeUID) throws FreeboxException {
        NodeConfigurationBuilder builder = NodeConfigurationBuilder.getInstance();
        try {
            homeManager.getHomeNodes().forEach(
                    node -> builder.configure(bridgeUID, node).ifPresent(result -> thingDiscovered(result.build())));
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover Home {}", e.getPermission());
        }
    }

    private void discoverPlugs(FreeplugManager freeplugManager, ThingUID bridgeUID) {
        FreeplugConfigurationBuilder builder = FreeplugConfigurationBuilder.getInstance();
        try {
            freeplugManager.getPlugs().forEach(plug -> thingDiscovered(builder.configure(bridgeUID, plug).build()));
        } catch (FreeboxException e) {
            logger.warn("Error discovering freeplugs {}", e.getMessage());
        }
    }

    private void discoverPhone(PhoneManager phoneManager, ThingUID bridgeUID) throws FreeboxException {
        PhoneConfigurationBuilder builder = PhoneConfigurationBuilder.getInstance();
        List<Status> statuses = List.of();
        try {
            statuses = phoneManager.getPhoneStatuses();
            statuses.forEach(phone -> thingDiscovered(builder.configure(bridgeUID, phone).build()));
        } catch (FreeboxException e) {
            logger.warn("Error discovering phones {}", e.getMessage());
        }
        if (!statuses.isEmpty()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_CALL, bridgeUID, "landline");
            logger.debug("Adding new Call thing {} to inbox", thingUID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withLabel("Freebox Calls").build();
            thingDiscovered(discoveryResult);
        }
    }

    private void discoverHosts(FreeboxOsHandler localHandler, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            List<MACAddress> wifiMacs = new ArrayList<>();
            wifiMacs.addAll(localHandler.getManager(APManager.class).getStations().stream().map(Station::mac)
                    .collect(Collectors.toList()));
            wifiMacs.addAll(localHandler.getManager(RepeaterManager.class).getHosts().stream().map(LanHost::getMac)
                    .collect(Collectors.toList()));

            lanHosts.forEach(lanHost -> {
                MACAddress mac = lanHost.getMac();
                String macString = mac.toColonDelimitedString();
                ThingUID thingUID = new ThingUID(wifiMacs.contains(mac) ? THING_TYPE_WIFI_HOST : THING_TYPE_HOST,
                        bridgeUID, mac.toHexString(false));
                logger.debug("Adding new Freebox Network Host {} to inbox", thingUID);
                DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel(lanHost.getPrimaryName().orElse("Network Device %s".formatted(macString)))
                        .withTTL(300).withProperty(Thing.PROPERTY_MAC_ADDRESS, macString)
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);
                thingDiscovered(builder.build());
            });
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover Hosts {}", e.getPermission());
        }
    }

    private void discoverVM(VmManager vmManager, ThingUID bridgeUID, List<LanHost> lanHosts) throws FreeboxException {
        try {
            vmManager.getDevices().forEach(vm -> {
                MACAddress mac = vm.mac();
                lanHosts.removeIf(host -> host.getMac().equals(mac));

                ThingUID thingUID = new ThingUID(THING_TYPE_VM, bridgeUID, mac.toHexString(false));
                logger.debug("Adding new VM Device {} to inbox", thingUID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                        .withLabel("%s (VM)".formatted(vm.name())).withProperty(ClientConfiguration.ID, vm.id())
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac.toColonDelimitedString()).build();
                thingDiscovered(discoveryResult);
            });
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover VM {}", e.getPermission());
        }
    }

    private void discoverRepeater(RepeaterManager repeaterManager, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            List<Repeater> repeaters = repeaterManager.getDevices();
            repeaters.forEach(repeater -> {
                MACAddress mac = repeater.mainMac();
                lanHosts.removeIf(host -> host.getMac().equals(mac));

                ThingUID thingUID = new ThingUID(THING_TYPE_REPEATER, bridgeUID, Integer.toString(repeater.id()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel("Repeater %s".formatted(repeater.name()))
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac.toColonDelimitedString())
                        .withProperty(ClientConfiguration.ID, repeater.id())
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                thingDiscovered(discoveryResult);
            });
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover Repeater {}", e.getPermission());
        }
    }

    private void discoverServer(SystemManager systemManager, ThingUID bridgeUID) throws FreeboxException {
        try {
            Config config = systemManager.getConfig();

            ThingTypeUID targetType = config.boardName().startsWith("fbxgw7") ? THING_TYPE_DELTA
                    : THING_TYPE_REVOLUTION;
            ThingUID thingUID = new ThingUID(targetType, bridgeUID, config.serial());
            logger.debug("Adding new Freebox Server {} to inbox", thingUID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, config.mac())
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).withLabel(config.modelInfo().prettyName())
                    .build();
            thingDiscovered(discoveryResult);
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover Server {}", e.getPermission());
        }
    }

    private void discoverPlayer(PlayerManager playerManager, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            for (Player player : playerManager.getDevices()) {
                lanHosts.removeIf(host -> host.getMac().equals(player.mac()));
                ThingUID thingUID = new ThingUID(player.apiAvailable() ? THING_TYPE_ACTIVE_PLAYER : THING_TYPE_PLAYER,
                        bridgeUID, Integer.toString(player.id()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel(player.deviceName())
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, player.mac().toColonDelimitedString())
                        .withProperty(ClientConfiguration.ID, player.id())
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                thingDiscovered(discoveryResult);
            }
        } catch (PermissionException e) {
            logger.warn("Missing permission to discover Player {}", e.getPermission());
        }
=======
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
import org.openhab.binding.freeboxos.internal.config.NodeConfiguration;
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
    private final Logger logger = LoggerFactory.getLogger(FreeboxOsDiscoveryService.class);
    private static final int DISCOVERY_TIME_SECONDS = 10;
    private static final int BACKGROUND_SCAN_REFRESH_MINUTES = 1;

    private @NonNullByDefault({}) FreeboxOsHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> backgroundFuture;

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
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                Map<String, LanHost> lanHosts = bridgeHandler.getManager(LanBrowserManager.class).getHostsMap();
                discoverServer(bridgeUID);
                discoverPhone(bridgeUID);
                discoverRepeater(bridgeUID, lanHosts);
                discoverPlayer(bridgeUID, lanHosts);
                discoverVM(bridgeUID, lanHosts);
                discoverHome(bridgeUID);
                if (bridgeHandler.getConfiguration().discoverNetDevice) {
                    discoverHosts(bridgeUID, lanHosts);
                }
            } catch (FreeboxException e) {
                logger.warn("Error while requesting data for things discovery : {}", e.getMessage());
            }
        }
    }

    private void discoverHome(ThingUID bridgeUID) throws FreeboxException {
        try {
            HomeManager homeManager = bridgeHandler.getManager(HomeManager.class);
            List<HomeNode> homeNodes = homeManager.getHomeNodes();
            homeNodes.forEach(node -> {
                DiscoveryResultBuilder discoveryResultBuilder = NodeConfiguration.configure(bridgeUID, node);
                if (discoveryResultBuilder != null) {
                    DiscoveryResult discoveryResult = discoveryResultBuilder.withLabel(node.getLabel())
                            .withBridge(bridgeUID).build();
                    thingDiscovered(discoveryResult);
                }
            });
        } catch (MissingPermissionException e) {
            logger.warn("Mission permission to discover Home {}", e.getPermission());
        }
    }

    private void discoverPhone(ThingUID bridgeUID) throws FreeboxException {
        try {
            PhoneManager phoneManager = bridgeHandler.getManager(PhoneManager.class);
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
            logger.warn("Mission permission to discover Phone {}", e.getPermission());
        }
    }

    private void discoverVM(ThingUID bridgeUID, Map<String, LanHost> lanHosts) throws FreeboxException {
        try {
            VmManager vmManager = bridgeHandler.getManager(VmManager.class);
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
            logger.warn("Mission permission to discover VM {}", e.getPermission());
        }
    }

    private void discoverHosts(ThingUID bridgeUID, Map<String, LanHost> lanHosts) throws FreeboxException {
        try {
            Map<String, AccessPointHost> apHosts = bridgeHandler.getManager(APManager.class).getHostsMap();
            Map<String, @Nullable LanHost> repHosts = bridgeHandler.getManager(RepeaterManager.class).getHostsMap();
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
            logger.warn("Mission permission to discover Hosts {}", e.getPermission());
        }
    }

    private void discoverRepeater(ThingUID bridgeUID, Map<String, LanHost> lanHosts) throws FreeboxException {
        try {
            List<Repeater> repeaters = bridgeHandler.getManager(RepeaterManager.class).getDevices();
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
            logger.warn("Mission permission to discover Repeater {}", e.getPermission());
        }
    }

    private void discoverServer(ThingUID bridgeUID) throws FreeboxException {
        try {
            SystemConf config = bridgeHandler.getManager(SystemManager.class).getConfig();

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
            logger.warn("Mission permission to discover Server {}", e.getPermission());
        }
    }

    private void discoverPlayer(ThingUID bridgeUID, Map<String, LanHost> lanHosts) throws FreeboxException {
        try {
            PlayerManager playMgr = bridgeHandler.getManager(PlayerManager.class);
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
            logger.warn("Mission permission to discover Player {}", e.getPermission());
        }
    }

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
>>>>>>> 46dadb1 SAT warnings handling
    }
}
