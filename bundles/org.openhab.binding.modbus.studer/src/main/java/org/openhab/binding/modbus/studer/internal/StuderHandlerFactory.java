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
package org.openhab.binding.modbus.studer.internal;

import static org.openhab.binding.modbus.studer.internal.StuderBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StuderHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Giovanni Mirulla - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.studer", service = ThingHandlerFactory.class)
public class StuderHandlerFactory extends BaseThingHandlerFactory {
    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(StuderHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        Configuration config = thing.getConfiguration();
        int slaveAddress = 0;
        try {
            slaveAddress = ((BigDecimal) config.get(SLAVE_ADDRESS)).intValue();
        } catch (Exception e) {
            // Do nothing
        }
        int refresh = ((config.get(REFRESH) == null) ? ((BigDecimal) config.get(REFRESH)).intValue()
                : StuderConfiguration.getRefresh());
        return new StuderHandler(thing, thingTypeUID, slaveAddress, refresh);
    }
}
