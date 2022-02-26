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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NetatmoLocation;
import org.openhab.core.thing.Bridge;

/**
 * The {@link HomePropertyHelper} takes care of handling properties for things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomePropertyHelper extends PropertyHelper {
    private Set<FeatureArea> featuresArea = Set.of();

    public HomePropertyHelper(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected Map<String, String> internalGetProperties(Map<String, String> currentProperties, NAObject data) {
        Map<String, String> properties = new HashMap<>(super.internalGetProperties(currentProperties, data));
        if (data instanceof NAHomeData) {
            NAHomeData home = (NAHomeData) data;
            featuresArea = home.getFeatures();
            if (firstLaunch) {
                properties.put(PROPERTY_COUNTRY, home.getCountry().orElse("undefined"));
                properties.put(PROPERTY_TIMEZONE, home.getTimezone().orElse("undefined"));
                properties.put(GROUP_LOCATION, ((NetatmoLocation) home).getLocation().toString());

                FeatureArea.AS_SET.stream().filter(area -> area != FeatureArea.NONE)
                        .forEach(area -> properties.put(area.name(), Boolean.toString(featuresArea.contains(area))));
            }
        }
        return properties;
    }

    public boolean hasFeature(FeatureArea seeked) {
        return featuresArea.contains(seeked);
    }
}
