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
package org.openhab.binding.hdanywhere.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HDanywhereBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class HDanywhereBindingConstants {

    public static final String BINDING_ID = "hdanywhere";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MULTIROOMPLUS = new ThingTypeUID(BINDING_ID, "multiroomplus");
    public static final ThingTypeUID THING_TYPE_MHUB4K431 = new ThingTypeUID(BINDING_ID, "mhub4k431");

    // List of all Channel ids
    public enum Port {
        ONE(1, "port1"),
        TWO(2, "port2"),
        THREE(3, "port3"),
        FOUR(4, "port4"),
        FIVE(5, "port5"),
        SIX(6, "port6"),
        SEVEN(7, "port7"),
        EIGHT(8, "port8");

        private final int number;
        private final String id;

        private Port(final int number, final String id) {
            this.number = number;
            this.id = id;
        }

        @Override
        public String toString() {
            return String.valueOf(number);
        }

        public int toNumber() {
            return number;
        }

        public static Port get(int valueSelectorNumber) throws IllegalArgumentException {
            for (Port c : Port.values()) {
                if (c.number == valueSelectorNumber) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }

        public static Port get(String valueSelectorText) throws IllegalArgumentException {
            for (Port c : Port.values()) {
                if (c.id.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }

        public String channelID() {
            return this.id;
        }
    }
}
