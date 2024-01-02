/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal.handler.utils;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This class support handling of openHAB type {@link State}. Therefore, it provides the methods:
 * <UL>
 * <LI>{@link #createState} for creating an openHAB {@link State}.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class StateUtils {

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-instantiability

    private StateUtils() {
        throw new AssertionError();
    }

    /**
     * Creates an openHAB {@link State} in accordance to the class of the given {@code propertyValue}. Currently
     * {@link PercentType}, {@link DecimalType}, and {@link Boolean} are handled explicitly. All other
     * {@code dataTypes} are mapped to {@link StringType}.
     * <p>
     * If {@code propertyValue} is {@code null}, {@link UnDefType#NULL} will be returned.
     * <P>
     * Copied/adapted from the org.openhab.binding.koubachi binding.
     * </P>
     *
     * @param propertyValue which should be converted,
     * @return <b>state</B> of type {@link State} in accordance with {@code dataType}. Will never be {@code null}.
     */
    public static State createState(@Nullable Object propertyValue) {
        if (propertyValue == null) {
            return UnDefType.NULL;
        }

        Class<?> dataType = propertyValue.getClass();

        if (PercentType.class.isAssignableFrom(dataType)) {
            return new PercentType((Integer) propertyValue);
        } else if (Integer.class.isAssignableFrom(dataType)) {
            return new DecimalType((Integer) propertyValue);
        } else if (BigDecimal.class.isAssignableFrom(dataType)) {
            return new DecimalType((BigDecimal) propertyValue);
        } else if (Boolean.class.isAssignableFrom(dataType)) {
            if ((Boolean) propertyValue) {
                return OnOffType.ON;
            } else {
                return OnOffType.OFF;
            }
        } else if (State.class.isAssignableFrom(dataType)) {
            return (State) propertyValue;
        } else {
            return new StringType(propertyValue.toString());
        }
    }
}
