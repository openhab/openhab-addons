/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wolfsmartset.internal.discovery;

import static org.openhab.binding.wolfsmartset.internal.WolfSmartsetBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wolfsmartset.internal.dto.GetSystemListDTO;
import org.openhab.binding.wolfsmartset.internal.dto.SubMenuEntryWithMenuItemTabView;
import org.openhab.binding.wolfsmartset.internal.handler.WolfSmartsetSystemBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WolfSmartsetAccountDiscoveryService} is responsible for discovering the WolfSmartset Units
 * that are associated with the WolfSmartset System
 *
 * @author Bo Biene - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = WolfSmartsetSystemDiscoveryService.class)
@NonNullByDefault
public class WolfSmartsetSystemDiscoveryService
        extends AbstractThingHandlerDiscoveryService<WolfSmartsetSystemBridgeHandler> {

    private final Logger logger = LoggerFactory.getLogger(WolfSmartsetSystemDiscoveryService.class);

    private @Nullable Future<?> discoveryJob;

    public WolfSmartsetSystemDiscoveryService() {
        super(WolfSmartsetSystemBridgeHandler.class, SUPPORTED_SYSTEM_AND_UNIT_THING_TYPES_UIDS, 8, true);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_SYSTEM_AND_UNIT_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("WolfSmartsetSystemDiscovery: Starting background discovery job");
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob == null || localDiscoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(() -> this.backgroundDiscover(),
                    DISCOVERY_INITIAL_DELAY_SECONDS, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.debug("WolfSmartsetSystemDiscovery: Stopping background discovery job");
        Future<?> localDiscoveryJob = discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    public void startScan() {
        logger.debug("WolfSmartsetSystemDiscovery: Starting discovery scan");
        discover();
    }

    private void backgroundDiscover() {
        var accountBridgeHandler = thingHandler.getAccountBridgeHandler();
        if (accountBridgeHandler == null || !accountBridgeHandler.isBackgroundDiscoveryEnabled()) {
            return;
        }
        discover();
    }

    private void discover() {
        if (thingHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("WolfSmartsetSystemDiscovery: Skipping discovery because Account Bridge thing is not ONLINE");
            return;
        }
        logger.debug("WolfSmartsetSystemDiscovery: Discovering WolfSmartset devices");
        discoverUnits();
    }

    private synchronized void discoverUnits() {
        String systemId = thingHandler.getSystemId();
        var systemConfig = thingHandler.getSystemConfig();
        if (systemConfig != null) {
            logger.debug("WolfSmartsetSystemDiscovery: Discovering units for system '{}' (id {})",
                    systemConfig.getName(), systemId);
            for (var unit : thingHandler.getUnits()) {
                ThingUID bridgeUID = thingHandler.getThing().getUID();
                ThingUID unitUID = new ThingUID(UID_UNIT_THING, bridgeUID, unit.menuItemTabViewDTO.bundleId.toString());
                thingDiscovered(createUnitDiscoveryResult(unitUID, bridgeUID, systemConfig, unit));
                logger.debug(
                        "WolfSmartsetSystemDiscovery: Unit for '{}' with id '{}' and name '{}' added with UID '{}'",
                        systemId, unit.menuItemTabViewDTO.bundleId, unit.menuItemTabViewDTO.tabName, unitUID);
            }
        }
    }

    private DiscoveryResult createUnitDiscoveryResult(ThingUID unitUID, ThingUID bridgeUID,
            GetSystemListDTO systemConfig, SubMenuEntryWithMenuItemTabView unit) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_UNIT_ID, unit.menuItemTabViewDTO.bundleId.toString());
        var tabName = unit.menuItemTabViewDTO.tabName;
        var menuName = unit.subMenuEntryDTO.getName();
        tabName = tabName.isEmpty() || "NULL".equalsIgnoreCase(tabName) || menuName.equalsIgnoreCase(tabName) ? ""
                : "-" + tabName;

        return DiscoveryResultBuilder.create(unitUID).withProperties(properties)
                .withRepresentationProperty(CONFIG_UNIT_ID).withBridge(bridgeUID)
                .withLabel(String.format("%s %s%s", systemConfig.getName(), menuName, tabName)).build();
    }
}
