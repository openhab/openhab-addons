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
package org.openhab.binding.netatmo.internal.handler.propertyhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.VENDOR;
import static org.openhab.core.thing.Thing.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.thing.Bridge;

/**
 * The {@link PropertyHelper} takes care of handling properties for things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PropertyHelper {
    private final ModuleType moduleType;
    protected final Bridge bridge;
    protected boolean firstLaunch;

    public PropertyHelper(Bridge bridge) {
        this.bridge = bridge;
        moduleType = ModuleType.valueOf(bridge.getThingTypeUID().getId());
    }

    public void setNewData(NAObject data) {
        Map<String, String> properties = bridge.getProperties();
        firstLaunch = properties.isEmpty();
        properties = internalGetProperties(properties, data);
        if (!bridge.getProperties().equals(properties)) {
            bridge.setProperties(properties);
        }
    }

    protected Map<String, String> internalGetProperties(Map<String, String> currentProperties, NAObject data) {
        Map<String, String> properties = new HashMap<>(currentProperties);
        if (firstLaunch && !moduleType.isLogical()) {
            properties.put(PROPERTY_VENDOR, VENDOR);
            properties.put(PROPERTY_MODEL_ID, moduleType.name());
        }
        if (data instanceof NAThing && !moduleType.isLogical()) {
            String firmware = ((NAThing) data).getFirmware();
            String current = properties.get(PROPERTY_FIRMWARE_VERSION);
            if (!firmware.equals(current)) {
                properties.put(PROPERTY_FIRMWARE_VERSION, firmware);
            }
        }
        return properties;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }
}
