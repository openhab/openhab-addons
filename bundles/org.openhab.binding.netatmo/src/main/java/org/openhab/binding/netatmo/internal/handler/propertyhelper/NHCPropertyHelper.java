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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.core.thing.Bridge;

/**
 * The {@link NHCPropertyHelper} takes care of handling properties for things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NHCPropertyHelper extends PropertyHelper {

    public NHCPropertyHelper(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected Map<String, String> internalGetProperties(Map<String, String> currentProperties, NAObject data) {
        Map<String, String> properties = new HashMap<>(super.internalGetProperties(currentProperties, data));
        if (data instanceof NAMain && firstLaunch) {
            ((NAMain) data).getPlace().ifPresent(place -> {
                properties.put(PROPERTY_CITY, place.getCity().orElse("undefined"));
                properties.put(PROPERTY_COUNTRY, place.getCountry().orElse("undefined"));
                properties.put(PROPERTY_TIMEZONE, place.getTimezone().orElse("undefined"));
            });
        }
        return properties;
    }
}
