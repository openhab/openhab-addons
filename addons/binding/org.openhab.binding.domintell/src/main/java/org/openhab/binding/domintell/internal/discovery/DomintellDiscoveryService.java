/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.discovery;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.domintell.internal.DomintellBindingConstants;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.model.Discoverable;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroupType;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

/**
 * Discovery service for bridge and modules
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(service = {DiscoveryService.class, DomintellDiscoveryService.class}, immediate = true, configurationPid = "discovery.domintell")
public class DomintellDiscoveryService extends AbstractDiscoveryService {
    private static final Map<ModuleType, ThingTypeUID> MODULE_THING_TYPES = new HashMap<ModuleType, ThingTypeUID>() {{
        put(ModuleType.TE1, THING_TYPE_MODULE_TEX);
        put(ModuleType.TE2, THING_TYPE_MODULE_TEX);
        put(ModuleType.BIR, THING_TYPE_MODULE_BIR);
        put(ModuleType.DMR, THING_TYPE_MODULE_DMR);
        put(ModuleType.IS4, THING_TYPE_MODULE_IS4);
        put(ModuleType.IS8, THING_TYPE_MODULE_IS8);
        put(ModuleType.B81, THING_TYPE_MODULE_PBX);
        put(ModuleType.B82, THING_TYPE_MODULE_PBX);
        put(ModuleType.B84, THING_TYPE_MODULE_PBX);
        put(ModuleType.B86, THING_TYPE_MODULE_PBX);
        put(ModuleType.BR2, THING_TYPE_MODULE_PBX);
        put(ModuleType.BR4, THING_TYPE_MODULE_PBX);
        put(ModuleType.BR6, THING_TYPE_MODULE_PBX);
        put(ModuleType.BU1, THING_TYPE_MODULE_PBX);
        put(ModuleType.BU2, THING_TYPE_MODULE_PBX);
        put(ModuleType.BU4, THING_TYPE_MODULE_PBX);
        put(ModuleType.BU6, THING_TYPE_MODULE_PBX);
        put(ModuleType.DIM, THING_TYPE_MODULE_DIM);
        put(ModuleType.D10, THING_TYPE_MODULE_D10);
    }};

    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellDiscoveryService.class);

    private static final int TIMEOUT = 5;

    /**
     * Domintell connection
     */
    private DomintellConnection domintellConnection;

    /**
     * Bridge UID
     */
    private ThingUID bridgeUID;

    /**
     * Domintell item groups
     */
    private Set<ItemGroup> itemGroups = new HashSet<>();

    /**
     * Domintell modules
     */
    private Set<Module> modules = new HashSet<>();

    /**
     * Constructor.
     */
    public DomintellDiscoveryService() {
        super(DomintellBindingConstants.BRIDGE_THING_TYPES_UIDS, TIMEOUT, true);
        logger.debug("Domintell discovery service created.");
    }

    /**
     * Add new bridge.
     */
    private void createResults() {
        removeOlderResults(new Date().getTime(), bridgeUID);
        if (bridgeUID == null) {
            createBridgeResult();
        } else {
            new HashSet<>(itemGroups).forEach(this::createGroupResult);
            new HashSet<>(modules).forEach(this::createModuleResult);
        }
    }

    private void createBridgeResult() {
        ThingUID uid = new ThingUID(BRIDGE_THING_TYPE, DomintellBindingConstants.DETH02);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid)
                .withThingType(BRIDGE_THING_TYPE)
                .withLabel("Domintell Bridge")
                .build();
        thingDiscovered(discoveryResult);
        logger.debug("Bridge discovered.");
    }

    private void createGroupResult(ItemGroup group) {
        ThingUID groupUID = getGroupTypeUID(group);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(groupUID)
                .withBridge(bridgeUID)
                .withLabel(group.getType().getName())
                .build();
        thingDiscovered(discoveryResult);
        logger.debug("Item group discovered: {}", group);
    }

    /**
     * Create discovery result from module
     *
     * @param module The module
     */
    private void createModuleResult(Module module) {
        ModuleKey moduleKey = module.getModuleKey();

        Configuration config = new Configuration();
        config.put(CONFIG_SERIAL_NUMBER, module.getModuleKey().getSerialNumber().getAddressInt());
        config.put(CONFIG_MODULE_TYPE, moduleKey.getModuleType().toString());

        ThingUID moduleUID = new ThingUID(MODULE_THING_TYPES.get(moduleKey.getModuleType()), bridgeUID, moduleKey.getSerialNumber().getAddressHex());
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(moduleUID)
                .withBridge(bridgeUID)
                .withLabel("Domintell " + moduleKey.toLabel())
                .withProperty(CONFIG_SERIAL_NUMBER, module.getModuleKey().getSerialNumber().getAddressInt())
                .withProperty(CONFIG_MODULE_TYPE, moduleKey.getModuleType().toString())
                .build();
        thingDiscovered(discoveryResult);
        logger.debug("Module discovered: {}", module.getModuleKey());
    }

    /**
     * Create thing uid for group
     *
     * @param group The group
     * @return Thing uid for group
     */
    private ThingUID getGroupTypeUID(ItemGroup group) {
        switch (group.getType()) {
            case system:
                return new ThingUID(THING_TYPE_GROUP, bridgeUID, ItemGroupType.system.toString());
            case variable:
                return new ThingUID(THING_TYPE_GROUP, bridgeUID, ItemGroupType.variable.toString());
        }
        throw new IllegalStateException("Item group not supported: " + group);
    }

    /**
     * Register new group for discovery
     *
     * @param group Discovered item group.
     */
    private void addGroup(ItemGroup group) {
        itemGroups.add(group);
        if (isBackgroundDiscoveryEnabled()) {
            createGroupResult(group);
        }
    }

    /**
     * Discover new module.
     *
     * @param module Discovered module.
     */
    private void addModule(Module module) {
        modules.add(module);
        if (isBackgroundDiscoveryEnabled()) {
            createModuleResult(module);
        }
    }

    /**
     * Remove item group.
     *
     * @param group Discovered channel group.
     */
    public void removeGroup(ItemGroup group) {
        itemGroups.remove(group);
        group.setDiscovered(true);
    }

    /**
     * Remove module.
     *
     * @param module Discovered module.
     */
    public void removeModule(Module module) {
        modules.remove(module);
        module.setDiscovered(true);
    }

    public void setBridgeUID(ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
        if (bridgeUID == null && isBackgroundDiscoveryEnabled()) {
            removeOlderResults(new Date().getTime());
            createBridgeResult();
        }
    }

    /**
     * Sets Domintell connection. Called after the bridge handler was instantiated.
     *
     * @param domintellConnection Connection
     */
    public void setDomintellConnection(DomintellConnection domintellConnection) {
        this.domintellConnection = domintellConnection;
    }

    /**
     * Create discovery result from registered groups and modules and also fires APPINFO command to scan the Domintell installation in the background
     */
    @Override
    protected void startScan() {
        createResults();
        if (domintellConnection != null) {
            domintellConnection.scan();
        }
    }

    @Override
    @Deactivate
    protected void deactivate() {
        removeOlderResults(new Date().getTime());
        super.deactivate();
    }

    @Override
    protected void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Register discoverable groups and modules.
     *
     * @param discoverable Object to register
     */
    public void addDiscoverable(Discoverable discoverable) {
        if (discoverable.isDiscoverable() && !discoverable.isDiscovered()) {
            if (discoverable instanceof ItemGroup) {
                addGroup((ItemGroup) discoverable);
            } else if (discoverable instanceof Module) {
                addModule((Module) discoverable);
            }
        }
    }
}
