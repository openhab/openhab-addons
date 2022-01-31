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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class ItemTestHelper {

    public static NumberItem createNumberItem(String name, Number value) {
        NumberItem numberItem = new NumberItem(name);
        if (value instanceof Integer || value instanceof Long) {
            numberItem.setState(new DecimalType(value.longValue()));
        } else {
            numberItem.setState(new DecimalType(value.doubleValue()));
        }
        return numberItem;
    }
}
