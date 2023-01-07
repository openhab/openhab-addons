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
package org.openhab.binding.irtrans.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IRtransBindingConstants} contains constants used by the IRtrans
 * handler classes
 *
 * @author Karel Goderis - Initial contribution
 *
 **/
public class IRtransBindingConstants {

    public static final String BINDING_ID = "irtrans";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ETHERNET_BRIDGE = new ThingTypeUID(BINDING_ID, "ethernet");
    public static final ThingTypeUID THING_TYPE_BLASTER = new ThingTypeUID(BINDING_ID, "blaster");

    // List of all Channel ids
    public static final String CHANNEL_IO = "io";

    // List of all Channel types
    public static final String BLASTER_CHANNEL_TYPE = "blaster";
    public static final String RECEIVER_CHANNEL_TYPE = "receiver";

    // List of possible leds on an IRtrans transceiver
    public enum Led {
        DEFAULT("D"),
        INTERNAL("I"),
        EXTERNAL("E"),
        ALL("B"),
        ONE("1"),
        TWO("2"),
        THREE("3"),
        FOUR("4"),
        FIVE("5"),
        SIX("6"),
        SEVEN("7"),
        EIGHT("8");

        private final String text;

        private Led(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static Led get(String valueSelectorText) throws IllegalArgumentException {
            for (Led c : Led.values()) {
                if (c.text.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not valid value selector");
        }
    }
}
