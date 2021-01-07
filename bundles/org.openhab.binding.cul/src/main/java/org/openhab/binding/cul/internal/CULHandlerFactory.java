/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal;

import static org.openhab.binding.cul.internal.CULBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.internal.handler.CulMaxBridgeHandler;
import org.openhab.binding.cul.internal.handler.CunMaxBridgeHandler;
import org.openhab.binding.cul.internal.network.CULNetworkConfigFactory;
import org.openhab.binding.cul.internal.network.CULNetworkHandlerImpl;
import org.openhab.binding.cul.internal.serial.CULSerialConfigFactory;
import org.openhab.binding.cul.internal.serial.CULSerialHandlerImpl;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CULHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Johannes Goehr - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.cul", service = ThingHandlerFactory.class)
public class CULHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(CULHandlerFactory.class);

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (CULMORIZBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID culBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, culBridgeUID, null);
        }
        if (CUNMORIZBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID culBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, culBridgeUID, null);
        }
        if (supportsThingType(thingTypeUID) && bridgeUID != null) {
            ThingUID deviceUID = getMaxCulDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber);
        }
        return thingUID;
    }

    private ThingUID getMaxCulDeviceUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String serialNumber = (String) configuration.get(Thing.PROPERTY_SERIAL_NUMBER);
            return new ThingUID(thingTypeUID, serialNumber, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (CULMORIZBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            CULManager manager = CULManager.getInstance();
            manager.registerHandlerClass(CULSerialConfigFactory.DEVICE_TYPE, CULSerialHandlerImpl.class,
                    new CULSerialConfigFactory(), new Class[] { SerialPortManager.class },
                    new Object[] { serialPortManager });
            return new CulMaxBridgeHandler((Bridge) thing, manager);
        } else if (CUNMORIZBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            CULManager manager = CULManager.getInstance();
            manager.registerHandlerClass(CULNetworkConfigFactory.DEVICE_TYPE, CULNetworkHandlerImpl.class,
                    new CULNetworkConfigFactory(), new Class[0], new Object[0]);
            return new CunMaxBridgeHandler((Bridge) thing, manager);
        } else {
            logger.debug("ThingHandler not found for {}", thingTypeUID);
            return null;
        }
    }
}
