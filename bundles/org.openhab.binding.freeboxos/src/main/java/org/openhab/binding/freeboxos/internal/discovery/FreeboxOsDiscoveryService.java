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
package org.openhab.binding.freeboxos.internal.discovery;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.PhoneType;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.MissingPermissionException;
import org.openhab.binding.freeboxos.internal.api.freeplug.FreeplugManager;
import org.openhab.binding.freeboxos.internal.api.home.HomeManager;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.lan.browser.LanHost;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.api.player.Player;
import org.openhab.binding.freeboxos.internal.api.player.PlayerManager;
import org.openhab.binding.freeboxos.internal.api.repeater.Repeater;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.system.SystemConfig;
import org.openhab.binding.freeboxos.internal.api.system.SystemManager;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.AccessPointManager;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.WifiStation;
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
                List<LanHost> lanHosts = localHandler.getManager(LanBrowserManager.class).getHosts();
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
        try {
            NodeConfigurationBuilder builder = NodeConfigurationBuilder.getInstance();
            homeManager.getHomeNodes().forEach(
                    node -> builder.configure(bridgeUID, node).ifPresent(result -> thingDiscovered(result.build())));
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Home {}", e.getPermission());
        }
    }

    private void discoverPlugs(FreeplugManager freeplugManager, ThingUID bridgeUID) {
        try {
            FreeplugConfigurationBuilder builder = FreeplugConfigurationBuilder.getInstance();
            freeplugManager.getPlugs().forEach(plug -> thingDiscovered(builder.configure(bridgeUID, plug).build()));
        } catch (FreeboxException e) {
            logger.warn("Error discovering freeplugs {}", e.getMessage());
        }
    }

    private void discoverPhone(PhoneManager phoneManager, ThingUID bridgeUID) throws FreeboxException {
        try {
            List<PhoneStatus> statuses = phoneManager.getPhoneStatuses();
            statuses.forEach(config -> {
                ThingUID thingUID = new ThingUID(config.getType() == PhoneType.DECT ? THING_TYPE_DECT : THING_TYPE_FXS,
                        bridgeUID, Integer.toString(config.getId()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withProperty(ClientConfiguration.ID, config.getId()).withLabel(config.getType().name())
                        .build();
                logger.debug("Adding new Freebox Phone {} to inbox", thingUID);
                thingDiscovered(discoveryResult);
            });
            if (!statuses.isEmpty()) {
                ThingUID thingUID = new ThingUID(THING_TYPE_CALL, bridgeUID, "landline");
                logger.debug("Adding new Freebox Phone {} to inbox", thingUID);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel("Freebox Calls").build();
                thingDiscovered(discoveryResult);
            }
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Phone {}", e.getPermission());
        }
    }

    private void discoverVM(VmManager vmManager, ThingUID bridgeUID, List<LanHost> lanHosts) throws FreeboxException {
        try {
            vmManager.getDevices().forEach(vm -> {
                String mac = vm.getMac();
                lanHosts.removeIf(host -> mac.equals(host.getMac()));

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

    private void discoverHosts(FreeboxOsHandler localHandler, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            List<WifiStation> apStations = localHandler.getManager(AccessPointManager.class).getStations();
            List<LanHost> repHosts = localHandler.getManager(RepeaterManager.class).getHosts();
            List<String> wifiMacs = Stream
                    .concat(apStations.stream().map(WifiStation::getMac), repHosts.stream().map(LanHost::getMac))
                    .collect(Collectors.toList());

            lanHosts.forEach(lanHost -> {
                if (lanHost.isReachable()) {
                    String mac = Objects.requireNonNull(lanHost.getMac());

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

    private void discoverRepeater(RepeaterManager repeaterManager, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            List<Repeater> repeaters = repeaterManager.getDevices();
            repeaters.forEach(repeater -> {
                String mac = repeater.getMainMac();
                lanHosts.removeIf(host -> mac.equals(host.getMac()));

                ThingUID thingUID = new ThingUID(THING_TYPE_REPEATER, bridgeUID, Integer.toString(repeater.getId()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel("Repeater %s".formatted(repeater.getName()))
                        .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac)
                        .withProperty(ClientConfiguration.ID, repeater.getId())
                        .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
                thingDiscovered(discoveryResult);
            });
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Repeater {}", e.getPermission());
        }
    }

    private void discoverServer(SystemManager systemManager, ThingUID bridgeUID) throws FreeboxException {
        try {
            SystemConfig config = systemManager.getConfig();

            ThingTypeUID targetType = config.getBoardName().startsWith("fbxgw7") ? THING_TYPE_DELTA
                    : THING_TYPE_REVOLUTION;
            ThingUID thingUID = new ThingUID(targetType, bridgeUID, config.getSerial());
            logger.debug("Adding new Freebox Server {} to inbox", thingUID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperty(Thing.PROPERTY_MAC_ADDRESS, config.getMac())
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
                    .withLabel(config.getModelInfo().getPrettyName()).build();
            thingDiscovered(discoveryResult);
        } catch (MissingPermissionException e) {
            logger.warn("Missing permission to discover Server {}", e.getPermission());
        }
    }

    private void discoverPlayer(PlayerManager playerManager, ThingUID bridgeUID, List<LanHost> lanHosts)
            throws FreeboxException {
        try {
            for (Player player : playerManager.getDevices()) {
                lanHosts.removeIf(host -> player.getMac().equals(host.getMac()));
                ThingUID thingUID = new ThingUID(player.isApiAvailable() ? THING_TYPE_ACTIVE_PLAYER : THING_TYPE_PLAYER,
                        bridgeUID, Integer.toString(player.getId()));
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                        .withLabel(player.getDeviceName()).withProperty(Thing.PROPERTY_MAC_ADDRESS, player.getMac())
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
