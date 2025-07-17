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
package org.openhab.binding.zwavejs.internal.discovery;

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.handler.ZwaveJSBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NodeDiscoveryService} tracks for Z-Wave nodes which are connected
 * to a Z-Wave JS webservice.
 *
 * @author Leo Siepel - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = NodeDiscoveryService.class)
@NonNullByDefault
public class NodeDiscoveryService extends AbstractThingHandlerDiscoveryService<ZwaveJSBridgeHandler> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_NODE);

    private static final int SEARCH_TIME = 10;

    private final Logger logger = LoggerFactory.getLogger(NodeDiscoveryService.class);

    /*
     * Creates an NodeDiscoveryService with enabled autostart.
     */
    @Activate
    public NodeDiscoveryService() {
        super(ZwaveJSBridgeHandler.class, SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Reference(unbind = "-")
    public void bindTranslationProvider(TranslationProvider translationProvider) {
        this.i18nProvider = translationProvider;
    }

    @Reference(unbind = "-")
    public void bindLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        thingHandler.registerDiscoveryListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        removeOlderResults(Instant.now(), getBridgeUID());
        thingHandler.unregisterDiscoveryListener();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        thingHandler.getFullState();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan(), getBridgeUID());
    }

    public void addNodeDiscovery(@Nullable Node node) {
        if (node == null) {
            logger.warn("Node discovery called with empty node.");
            return;
        }
        ThingUID thingUID = getThingUID(node.nodeId);
        ThingTypeUID thingTypeUID = THING_TYPE_NODE;
        logger.trace("Node {}. addNodeDiscovery", node.nodeId);

        if (thingUID != null) {
            String manufacturer = node.deviceConfig != null ? node.deviceConfig.manufacturer : "Unknown";
            String product = node.deviceConfig != null ? node.deviceConfig.label : "";
            String discoveryLabel = String.format(DISCOVERY_NODE_LABEL_PATTERN, manufacturer, product, node.nodeId);

            Map<String, Object> properties = new HashMap<>();

            properties.put(CONFIG_NODE_ID, node.nodeId);

            properties.put(PROPERTY_NODE_IS_LISTENING, node.isListening);
            properties.put(PROPERTY_NODE_IS_ROUTING, node.isRouting);
            properties.put(PROPERTY_NODE_IS_SECURE, node.isSecure);
            properties.put(PROPERTY_VENDOR, manufacturer);
            properties.put(PROPERTY_MODEL_ID, product);
            properties.put(PROPERTY_NODE_LASTSEEN, node.lastSeen);
            properties.put(PROPERTY_NODE_FREQ_LISTENING, node.isFrequentListening);
            properties.put(PROPERTY_FIRMWARE_VERSION, node.firmwareVersion);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(getBridgeUID()).withRepresentationProperty(CONFIG_NODE_ID)
                    .withLabel(discoveryLabel).build();

            thingDiscovered(discoveryResult);
        } else {
            logger.warn("Node {}. Discovered unsupported device.", node.nodeId);
        }
    }

    @Override
    public void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }

    public void removeNodeDiscovery(int nodeId) {
        ThingUID thingUID = getThingUID(nodeId);

        if (thingUID != null) {
            thingRemoved(thingUID);
            logger.debug("Node {}. removeNodeDiscovery", nodeId);
        }
    }

    private @Nullable ThingUID getBridgeUID() {
        return thingHandler.getThing().getUID();
    }

    private @Nullable ThingUID getThingUID(int nodeId) {
        ThingUID localBridgeUID = getBridgeUID();
        if (localBridgeUID != null) {
            return new ThingUID(BindingConstants.THING_TYPE_NODE, localBridgeUID, "node" + nodeId);
        }
        return null;
    }
}
