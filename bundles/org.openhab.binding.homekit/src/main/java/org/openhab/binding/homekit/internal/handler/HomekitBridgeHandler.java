/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.handler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.action.HomekitPairingActions;
import org.openhab.binding.homekit.internal.discovery.HomekitBridgedAccessoryDiscoveryService;
import org.openhab.binding.homekit.internal.dto.Characteristic;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;

/**
 * Handler for a HomeKit bridge accessory.
 * It marshals the communications with multiple HomeKit bridged accessories within a HomeKit bridge.
 * It notifies the {@link HomekitBridgedAccessoryDiscoveryService} when bridged accessories are discovered.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends HomekitBaseAccessoryHandler implements BridgeHandler {

    private @Nullable HomekitBridgedAccessoryDiscoveryService bridgedAccessoryDiscoveryService = null;

    public HomekitBridgeHandler(Bridge bridge, HomekitTypeProvider typeProvider, HomekitKeyStore keyStore,
            TranslationProvider i18nProvider, Bundle bundle) {
        super(bridge, typeProvider, keyStore, i18nProvider, bundle);
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    /**
     * Creates a bridge builder, which allows to modify the bridge. The 'updateThing(Thing)' method
     * must be called to persist the changes.
     *
     * @return {@link BridgeBuilder} which builds an exact copy of the bridge
     */
    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties())
                .withSemanticEquipmentTag(thing.getSemanticEquipmentTag());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HomekitBridgedAccessoryDiscoveryService.class, HomekitPairingActions.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // do nothing
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // do nothing
    }

    @Override
    protected boolean bridgedThingsInitialized() {
        return getThing().getThings().stream()
                .allMatch(bridgedAccessory -> ThingHandlerHelper.isHandlerInitialized(bridgedAccessory));
    }

    @Override
    protected void onConnectedThingAccessoriesLoaded() {
        createProperties();
        getThing().getThings().forEach(bridgedThing -> {
            if (bridgedThing.getHandler() instanceof HomekitAccessoryHandler accessoryHandler) {
                accessoryHandler.onConnectedThingAccessoriesLoaded();
            }
        });
    }

    @Override
    public void onEvent(String jsonContent) {
        getThing().getThings().forEach(bridgedThing -> {
            if (bridgedThing.getHandler() instanceof HomekitAccessoryHandler accessoryHandler) {
                accessoryHandler.onEvent(jsonContent);
            }
        });
    }

    @Override
    protected void onThingOnline() {
        updateStatus(ThingStatus.ONLINE);
        getThing().getThings().forEach(bridgedThing -> {
            if (bridgedThing.getHandler() instanceof HomekitAccessoryHandler accessoryHandler) {
                accessoryHandler.onThingOnline();
            }
        });
        super.onThingOnline();
        HomekitBridgedAccessoryDiscoveryService discoveryService = bridgedAccessoryDiscoveryService;
        if (discoveryService != null) {
            discoveryService.startScan();
        }
    }

    public void registerDiscoveryService(HomekitBridgedAccessoryDiscoveryService discoveryService) {
        bridgedAccessoryDiscoveryService = discoveryService;
    }

    public void unregisterDiscoveryService() {
        bridgedAccessoryDiscoveryService = null;
    }

    @Override
    protected Map<String, Characteristic> getEventedCharacteristics() {
        eventedCharacteristics.clear();
        getThing().getThings().forEach(bridgedThing -> {
            if (bridgedThing.getHandler() instanceof HomekitAccessoryHandler accessoryHandler) {
                eventedCharacteristics.putAll(accessoryHandler.getPolledCharacteristics());
            }
        });
        return eventedCharacteristics;
    }

    @Override
    protected Map<String, Characteristic> getPolledCharacteristics() {
        polledCharacteristics.clear();
        getThing().getThings().forEach(bridgedThing -> {
            if (bridgedThing.getHandler() instanceof HomekitAccessoryHandler accessoryHandler) {
                polledCharacteristics.putAll(accessoryHandler.getPolledCharacteristics());
            }
        });
        return polledCharacteristics;
    }
}
