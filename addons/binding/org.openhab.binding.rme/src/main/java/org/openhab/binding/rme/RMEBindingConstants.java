/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rme;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RMEBinding} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class RMEBindingConstants {

    public static final String BINDING_ID = "rme";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MANAGER = new ThingTypeUID(BINDING_ID, "manager");

    // List of all Channel ids
    public enum DataField {

        LEVEL("waterlevel", 1),
        MODE("mode", 2),
        SOURCE("source", 3),
        EXITPUMP("exitpump", 4),
        ENTRYPUMP("entrypump", 5),
        WATEREXCHANGE("waterexchange", 6),
        CISTERNSUPPLYALARM("cisternsupplyalarm", 7),
        OVERFLOWALARM("overflowalarm", 8),
        CISTERNBLOCKEDALARM("cisternblockedalarm", 9),
        FILTERCLEANING("filtercleaning", 10);

        private final String id;
        private final int number;

        private DataField(final String id, final int number) {
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

        public static DataField get(int valueSelectorNumber) throws IllegalArgumentException {
            for (DataField c : DataField.values()) {
                if (c.number == valueSelectorNumber) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid value selector");
        }

        public static DataField get(String valueSelectorText) throws IllegalArgumentException {
            for (DataField c : DataField.values()) {
                if (c.id.equals(valueSelectorText)) {
                    return c;
                }
            }

            throw new IllegalArgumentException("Not a valid value selector");
        }

        public String channelID() {
            return this.id;
        }

    }

}
