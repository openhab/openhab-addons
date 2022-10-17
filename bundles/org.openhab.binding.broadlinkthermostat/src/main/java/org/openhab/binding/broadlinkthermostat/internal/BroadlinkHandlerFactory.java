/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.broadlinkthermostat.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.broadlinkthermostat.internal.handler.FloureonThermostatHandler;
import org.openhab.binding.broadlinkthermostat.internal.handler.RMUniversalRemoteHandler;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BroadlinkHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Florian Mueller - Initial contribution
 */
@Component(configurationPid = "binding.broadlinkthermostat", service = ThingHandlerFactory.class)
@NonNullByDefault
public class BroadlinkHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(
            BroadlinkBindingConstants.FLOUREON_THERMOSTAT_THING_TYPE,
            BroadlinkBindingConstants.RM_UNIVERSAL_REMOTE_THING_TYPE,
            BroadlinkBindingConstants.HYSEN_THERMOSTAT_THING_TYPE);
    private static final String BROADLINK_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "broadlink").toString();
    public static final String INFRARED_FOLDER = Path.of(BROADLINK_FOLDER, "infrared_commands").toString();
    static {
        Logger logger = LoggerFactory.getLogger(BroadlinkHandlerFactory.class);
        File directory = new File(BROADLINK_FOLDER);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                logger.info("broadlink dir created {}", BROADLINK_FOLDER);
            }
        }
        File childDirectory = new File(INFRARED_FOLDER);
        if (!childDirectory.exists()) {
            if (childDirectory.mkdir()) {
                logger.info("infrared_commands dir created {}", INFRARED_FOLDER);
            }
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BroadlinkBindingConstants.FLOUREON_THERMOSTAT_THING_TYPE.equals(thingTypeUID)
                || BroadlinkBindingConstants.HYSEN_THERMOSTAT_THING_TYPE.equals(thingTypeUID)) {
            return new FloureonThermostatHandler(thing);
        }
        if (BroadlinkBindingConstants.RM_UNIVERSAL_REMOTE_THING_TYPE.equals(thingTypeUID)) {
            return new RMUniversalRemoteHandler(thing);
        }
        return null;
    }
}
