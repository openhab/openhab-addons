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
package org.openhab.binding.ahawastecollection.internal;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Contains the next collection dates for an given waste type.
 * 
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
final class CollectionDate {

    /**
     * Type of waste that is collected.
     */
    public enum WasteType {
        /**
         * General waste.
         */
        GENERAL_WASTE,
        /**
         * Bio waste.
         */
        BIO_WASTE,
        /**
         * Paper.
         */
        PAPER,
        /**
         * Light packaging.
         */
        LIGHT_PACKAGES;

        /**
         * Parses the {@link WasteType} from the given Value from the Web-page.
         */
        public static WasteType parseValue(String value) {
            switch (value) {
                case "Restabfall":
                    return GENERAL_WASTE;
                case "Bioabfall":
                    return BIO_WASTE;
                case "Papier":
                    return PAPER;
                case "Leichtverpackungen":
                    return LIGHT_PACKAGES;
                default:
                    throw new IllegalArgumentException("Unknown waste type: " + value);
            }
        }
    }

    private final WasteType type;
    private final List<Date> dates;

    /**
     * Creates an new {@link CollectionDate}.
     */
    public CollectionDate(final WasteType type, final List<Date> dates) {
        this.type = type;
        this.dates = dates;
        Collections.sort(this.dates);
    }

    /**
     * Returns the (non empty list) of next collection dates, for the given {@link WasteType}, ordered ascending.
     */
    public List<Date> getDates() {
        return this.dates;
    }

    /**
     * Returns the {@link WasteType} that is collected at the given times.
     */
    public WasteType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return String.format("waste type: %s, collection dates: %s", this.type, this.dates);
    }
}
