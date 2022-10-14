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
package org.openhab.persistence.jdbc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class manages strategy for table names.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class NamingStrategy {
    private static final String ITEM_NAME_PATTERN = "[^a-zA-Z_0-9\\-]";

    private JdbcConfiguration configuration;

    public NamingStrategy(JdbcConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getTableName(int rowId, String itemName) {
        if (configuration.getTableUseRealItemNames()) {
            return (itemName.replaceAll(ITEM_NAME_PATTERN, "")).toLowerCase();
        } else {
            return configuration.getTableNamePrefix() + formatRight(rowId, configuration.getTableIdDigitCount());
        }
    }

    private static String formatRight(final Object value, final int len) {
        final String valueAsString = String.valueOf(value);
        if (valueAsString.length() < len) {
            final StringBuffer result = new StringBuffer(len);
            for (int i = len - valueAsString.length(); i > 0; i--) {
                result.append('0');
            }
            result.append(valueAsString);
            return result.toString();
        } else {
            return valueAsString;
        }
    }
}
