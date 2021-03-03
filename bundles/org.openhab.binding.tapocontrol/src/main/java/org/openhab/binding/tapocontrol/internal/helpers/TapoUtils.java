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
package org.openhab.binding.tapocontrol.internal.helpers;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * {@link TapoUtils} TapoUtils -
 * Utility Helper Functions
 *
 * @author Christian Wild - Initial Initial contribution
 */
@NonNullByDefault
public class TapoUtils {

    /************************************
     * TYPE UTILS
     ***********************************/

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(@Nullable Boolean boolVal) {
        return (boolVal != null ? boolVal ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    /**
     * Return OnOffType from bool
     * 
     * @param boolVal
     */
    public static OnOffType getOnOffType(Integer intVal) {
        return intVal == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Return StringType from String
     * 
     * @param strVal
     */
    public static StringType getStringType(@Nullable String strVal) {
        return new StringType(strVal != null ? strVal : "");
    }

    /**
     * Return DecimalType from Double
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Double numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Integer
     * 
     * @param numVal
     */
    public static DecimalType getDecimalType(@Nullable Integer numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }

    /**
     * Return DecimalType from Longe
     * 
     * @param numVal
     */
    public static DecimalType getDecimalTypel(@Nullable Long numVal) {
        return new DecimalType((numVal != null ? numVal : 0));
    }
}
