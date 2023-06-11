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
package org.openhab.binding.caddx.internal.factory;

import static org.openhab.binding.caddx.internal.CaddxBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.handler.CaddxBridgeHandler;
import org.openhab.binding.caddx.internal.handler.ThingHandlerKeypad;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPanel;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPartition;
import org.openhab.binding.caddx.internal.handler.ThingHandlerZone;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
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
    private final SerialPortManager portManager;

    @Activate
    public CaddxHandlerFactory(@Reference SerialPortManager portManager) {
        this.portManager = portManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(CaddxBindingConstants.CADDXBRIDGE_THING_TYPE)) {
            return new CaddxBridgeHandler(portManager, (Bridge) thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.PANEL_THING_TYPE)) {
            return new ThingHandlerPanel(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.PARTITION_THING_TYPE)) {
            return new ThingHandlerPartition(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.ZONE_THING_TYPE)) {
            return new ThingHandlerZone(thing);
        } else if (thingTypeUID.equals(CaddxBindingConstants.KEYPAD_THING_TYPE)) {
            return new ThingHandlerKeypad(thing);
        } else {
            logger.debug("createHandler(): ThingHandler not found for {}", thingTypeUID);

            return null;
        }
    }
}
