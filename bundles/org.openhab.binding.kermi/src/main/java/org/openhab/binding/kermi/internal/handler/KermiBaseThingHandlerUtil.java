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
package org.openhab.binding.kermi.internal.handler;

import javax.measure.Unit;

import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.api.Config;
import org.openhab.binding.kermi.internal.model.KermiSiteInfoUtil;
import org.openhab.core.thing.type.ChannelTypeUID;

import tech.units.indriya.unit.Units;

/**
 * @author Marco Descher - Initial contribution
 */
public class KermiBaseThingHandlerUtil {

    public ChannelTypeUID determineChannelTypeUID(Config datapointConfig) {
        switch (datapointConfig.getDatapointType()) {
            case 0:
                // enumeration values, there should be a specific item-type for each
                // of these channels, we simply return the numeric value by now
                return KermiBindingConstants.CHANNEL_TYPE_NUMBER;
            case 1:
                Unit<?> unit = KermiSiteInfoUtil.determineUnitByString(datapointConfig.getUnit());
                if (Units.WATT.equals(unit)) {
                    return KermiBindingConstants.CHANNEL_TYPE_POWER;
                } else if (Units.CELSIUS.equals(unit)) {
                    return KermiBindingConstants.CHANNEL_TYPE_TEMPERATURE;
                }
                return KermiBindingConstants.CHANNEL_TYPE_NUMBER;
            case 2:
                return KermiBindingConstants.CHANNEL_TYPE_ONOFF;
            case 3:
                return KermiBindingConstants.CHANNEL_TYPE_STRING;
            case 4:
                // time value?
                return KermiBindingConstants.CHANNEL_TYPE_STRING;
            default:
                break;
        }
        return null;
    }
}
