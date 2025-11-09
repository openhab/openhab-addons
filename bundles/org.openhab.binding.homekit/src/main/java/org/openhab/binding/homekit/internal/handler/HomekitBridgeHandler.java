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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.action.HomekitPairingActions;
import org.openhab.binding.homekit.internal.discovery.HomekitChildDiscoveryService;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;

/**
 * Handler for HomeKit bridge devices.
 * It marshals the communications with multiple HomeKit child accessories within a HomeKit bridge server.
 * It uses the /accessories endpoint to discover embedded accessories and their services.
 * It notifies the {@link HomekitChildDiscoveryService} when accessories are discovered.
 * It does not currently handle commands for channels, that is left to the child accessory handlers.
 * It extends {@link HomekitBaseAccessoryHandler} to handle pairing and secure session setup.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends HomekitBaseAccessoryHandler implements BridgeHandler {

    private @Nullable HomekitChildDiscoveryService childDiscoveryService = null;

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
        return Set.of(HomekitChildDiscoveryService.class, HomekitPairingActions.class);
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
    protected boolean checkHandlersInitialized() {
        return getThing().getThings().stream().allMatch(child -> ThingHandlerHelper.isHandlerInitialized(child));
    }

    @Override
    protected void onAccessoriesLoaded() {
        createProperties();
        getThing().getThings().forEach(child -> {
            if (child.getHandler() instanceof HomekitBaseAccessoryHandler childHandler) {
                childHandler.onAccessoriesLoaded();
            }
        });
    }

    @Override
    protected void onRootHandlerReady() {
        eventedCharacteristics.clear();
        getThing().getThings().forEach(child -> {
            if (child.getHandler() instanceof HomekitBaseAccessoryHandler childHandler) {
                childHandler.onRootHandlerReady();
                eventedCharacteristics.addAll(childHandler.eventedCharacteristics);
            }
        });
    }

    @Override
    public void onEvent(String jsonContent) {
        getThing().getThings().forEach(child -> {
            if (child.getHandler() instanceof HomekitBaseAccessoryHandler childHandler) {
                childHandler.onEvent(jsonContent);
            }
        });
    }

    @Override
    protected void onThingOnline() {
        super.onThingOnline();
        HomekitChildDiscoveryService discoveryService = childDiscoveryService;
        if (discoveryService != null) {
            discoveryService.startScan();
        }
    }

    public void registerDiscoveryService(HomekitChildDiscoveryService discoveryService) {
        childDiscoveryService = discoveryService;
    }

    public void unregisterDiscoveryService() {
        childDiscoveryService = null;
    }
}
