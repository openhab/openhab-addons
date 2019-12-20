/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.openthermgateway.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link TypeConverter} is used to convert simple values to SmartHome types
 * sent to one of the channels.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class TypeConverter {
    /*
     * DateTimeType
     * DecimalType
     * HSBType
     * OnOffType
     * OpenClosedType
     * PercentType
     * PlayPauseType
     * PointType
     * RawType
     * RewindFastforwardType
     * StringListType
     * StringType
     * UnDefType
     * UpDownType
     */

    public static DecimalType toDecimalType(float value) {
        return new DecimalType(value);
    }

    public static OnOffType toOnOffType(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }

    public static PercentType toPercentType(int value) {
        return new PercentType(value);
    }

    public static StringType toStringType(String value) {
        return new StringType(value);
    }
}
