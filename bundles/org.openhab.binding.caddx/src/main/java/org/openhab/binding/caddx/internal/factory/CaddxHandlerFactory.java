/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.caddx.internal.factory;

import static org.openhab.binding.caddx.internal.CaddxBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.config.CaddxBridgeConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxPartitionConfiguration;
import org.openhab.binding.caddx.internal.config.CaddxZoneConfiguration;
import org.openhab.binding.caddx.internal.discovery.CaddxDiscoveryService;
import org.openhab.binding.caddx.internal.handler.CaddxBridgeHandler;
import org.openhab.binding.caddx.internal.handler.ThingHandlerKeypad;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPanel;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPartition;
import org.openhab.binding.caddx.internal.handler.ThingHandlerZone;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CaddxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@Component(configurationPid = "binding.caddx", service = ThingHandlerFactory.class)
@NonNullByDefault
public class CaddxHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(CaddxHandlerFactory.class);
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();
    private @NonNullByDefault({}) SerialPortManager portManager;

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        logger.trace("createThing(): thingTypeUID- {}, configuration - {}", thingTypeUID, configuration);

        if (CaddxBindingConstants.CADDXBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID caddxBridgeUID = getCaddxBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, caddxBridgeUID, null);
        } else if (CaddxBindingConstants.PANEL_THING_TYPE.equals(thingTypeUID)) {
            ThingUID panelThingUID = getCaddxPanelUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, panelThingUID, bridgeUID);
        } else if (CaddxBindingConstants.PARTITION_THING_TYPE.equals(thingTypeUID)) {
            ThingUID partitionThingUID = getCaddxPartitionUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, partitionThingUID, bridgeUID);
        } else if (CaddxBindingConstants.ZONE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID zoneThingUID = getCaddxZoneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zoneThingUID, bridgeUID);
        } else if (CaddxBindingConstants.KEYPAD_THING_TYPE.equals(thingTypeUID)) {
            ThingUID keypadThingUID = getCaddxKeypadUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, keypadThingUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "createThing(): The thing type " + thingTypeUID + " is not supported by the Caddx binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Get the Caddx Bridge Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @return thingUID
     */
    private @Nullable ThingUID getCaddxBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        ThingUID t = thingUID;
        if (thingUID == null) {
            String serialPort = (String) configuration.get(CaddxBridgeConfiguration.SERIAL_PORT);
            String bridgeID = serialPort.replace('.', '_');
            t = new ThingUID(thingTypeUID, bridgeID);
        }
        return t;
    }

    /**
     * Get the Panel Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getCaddxPanelUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        ThingUID t = thingUID;
        if (thingUID == null && bridgeUID != null) {
            String panelId = "panel";
            t = new ThingUID(thingTypeUID, panelId, bridgeUID.getId());
        }
        return t;
    }

    /**
     * Get the Partition Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getCaddxPartitionUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        ThingUID t = thingUID;
        if (thingUID == null && bridgeUID != null) {
            String partitionId = "partition" + (String) configuration.get(CaddxPartitionConfiguration.PARTITION_NUMBER);
            t = new ThingUID(thingTypeUID, partitionId, bridgeUID.getId());
        }
        return t;
    }

    /**
     * Get the Zone Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getCaddxZoneUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        ThingUID t = thingUID;
        if (thingUID == null && bridgeUID != null) {
            String zoneId = "zone" + (String) configuration.get(CaddxZoneConfiguration.ZONE_NUMBER);
            t = new ThingUID(thingTypeUID, zoneId, bridgeUID.getId());
        }
        return t;
    }

    /**
     * Get the Keypad Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getCaddxKeypadUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        ThingUID t = thingUID;
        if (thingUID == null && bridgeUID != null) {
            String keypadId = "keypad";
            t = new ThingUID(thingTypeUID, keypadId, bridgeUID.getId());
        }
        return t;
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param caddxBridgeHandler The Bridge handler
     */
    private void registerCaddxDiscoveryService(CaddxBridgeHandler caddxBridgeHandler) {
        CaddxDiscoveryService discoveryService = new CaddxDiscoveryService(caddxBridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(caddxBridgeHandler.getThing().getUID(), discoveryServiceRegistration);

        logger.trace("registerCaddxDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                caddxBridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(CaddxBindingConstants.CADDXBRIDGE_THING_TYPE)) {
            CaddxBridgeHandler handler = new CaddxBridgeHandler(portManager, (Bridge) thing);
            registerCaddxDiscoveryService(handler);

            logger.trace("createHandler(): BRIDGE_THING: ThingHandler created for {}", thingTypeUID);

            return handler;
        } else if (thingTypeUID.equals(CaddxBindingConstants.PANEL_THING_TYPE)) {
            logger.trace("createHandler(): PANEL_THING: ThingHandler created for {}", thingTypeUID);

            return new ThingHandlerPanel(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.PARTITION_THING_TYPE)) {
            logger.trace("createHandler(): PARTITION_THING: ThingHandler created for {}", thingTypeUID);

            return new ThingHandlerPartition(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.ZONE_THING_TYPE)) {
            logger.trace("createHandler(): ZONE_THING: ThingHandler created for {}", thingTypeUID);

            return new ThingHandlerZone(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.KEYPAD_THING_TYPE)) {
            logger.trace("createHandler(): KEYPAD_THING: ThingHandler created for {}", thingTypeUID);

            return new ThingHandlerKeypad(thing);
        } else {
            logger.trace("createHandler(): ThingHandler not found for {}", thingTypeUID);

            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ThingTypeUID thingTypeUID = thingHandler.getThing().getThingTypeUID();

        if (thingTypeUID.equals(CaddxBindingConstants.CADDXBRIDGE_THING_TYPE)) {
            ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations
                    .get(thingHandler.getThing().getUID());

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        }
        super.removeHandler(thingHandler);
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.portManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.portManager = null;
    }
}
