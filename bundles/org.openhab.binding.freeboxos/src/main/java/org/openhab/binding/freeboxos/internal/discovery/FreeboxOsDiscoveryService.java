/**
<<<<<<< Upstream, based on origin/main
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
<<<<<<< Upstream, based on origin/main
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
=======
 * Copyright (c) 2010-2023 Contributors to the openHAB project
>>>>>>> 006a813 Saving work before instroduction of ArrayListDeserializer
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

=======
>>>>>>> e4ef5cc Switching to Java 17 records
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
    }
<<<<<<< Upstream, based on origin/main

    private String macToUid(String mac) {
        return mac.replaceAll("[^A-Za-z0-9_]", "");
>>>>>>> 46dadb1 SAT warnings handling
    }
=======
>>>>>>> e4ef5cc Switching to Java 17 records
}
