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
package org.openhab.binding.hdpowerview.internal.database;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing the database of all known shade 'types', their respective 'capabilities', plus other relevant
 * functional attributes.
 *
 * If user systems detect shade types that are not in the database, then this class will issue logger warning messages
 * indicating such absence, and prompting the user to report it to developers so that the database and the respective
 * binding functionality can be extended over time.
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class ShadeCapabilitiesDatabase {

    private static final String NOT_DETERMINED = "Not determined";

    private final Logger logger = LoggerFactory.getLogger(ShadeCapabilitiesDatabase.class);

    /*
     * Database of known shade types
     */
    private static final TypeItem[] TYPE_DATABASE = {
        // @formatter:off
            new TypeItem( 4, 0, "Roman"),
            new TypeItem( 5, 0, "Bottom Up"),
            new TypeItem( 6, 0, "Duette"),
            new TypeItem( 7, 6, "Top Down"),
            new TypeItem( 8, 7, "Duette Top Down Bottom Up"),
            new TypeItem( 9, 7, "Duette DuoLite Top Down Bottom Up"),
            new TypeItem(23, 1, "Silhouette"),
            new TypeItem(42, 0, "M25T Roller Blind"),
            new TypeItem(43, 1, "Facette"),
            new TypeItem(44, 0, "Twist"),
            new TypeItem(47, 7, "Pleated Top Down Bottom Up"),
            new TypeItem(49, 0, "AC Roller"),
            new TypeItem(51, 2, "Venetian"),
            new TypeItem(54, 3, "Vertical Slats Left Stack"),
            new TypeItem(55, 3, "Vertical Slats Right Stack"),
            new TypeItem(56, 3, "Vertical Slats Split Stack"),
            new TypeItem(62, 2, "Venetian"),
            new TypeItem(69, 3, "Curtain Left Stack"),
            new TypeItem(70, 3, "Curtain Right Stack"),
            new TypeItem(71, 3, "Curtain Split Stack"),
            new TypeItem(79, 8, "Duolite Lift"),
            // =================================
            new TypeItem(-1, -1, NOT_DETERMINED)
        // @formatter:on
    };

    /*
     * Database of known shade capabilities
     */
    private static final CapabilitiesItem[] CAPABILITIES_DATABASE = {
        // @formatter:off
            new CapabilitiesItem(0, "Bottom Up"),
            new CapabilitiesItem(1, "Bottom Up Tilt 90°"),
            new CapabilitiesItem(2, "Bottom Up Tilt 180°"),
            new CapabilitiesItem(3, "Vertical"),
            new CapabilitiesItem(4, "Vertical Tilt 180°"),
            new CapabilitiesItem(5, "Tilt Only 180°"),
            new CapabilitiesItem(6, "Top Down").primaryReversed(),
            new CapabilitiesItem(7, "Top Down Bottom Up").supportsSecondary().notSupportsVane(),
            new CapabilitiesItem(8, "Duolite Lift"),
            new CapabilitiesItem(9, "Duolite Lift and Tilt 90°"),
            // =================================
            new CapabilitiesItem(-1, NOT_DETERMINED)
        // @formatter:on
    };

    /**
     * Describes a shade type entry in the known shades database. Includes the 'type' parameter, its respective
     * 'capabilities' parameter, and a description text
     *
     * @author AndrewFG - Initial contribution
     *
     */
    static class TypeItem {
        int type;
        int capabilities;
        String description;

        public TypeItem(int type, int capabilities, String description) {
            this.type = type;
            this.capabilities = capabilities;
            this.description = description;
        }
    }

    /**
     * Describes a shade 'capabilities' entry in the database. Includes the 'capabilities' parameter, its description,
     * and an indication whether the respective shade supports a secondary rail.
     *
     * @author AndrewFG - Initial contribution
     *
     */
    static class CapabilitiesItem {
        int capabilities;
        String description;
        boolean supportsVane = true;
        boolean supportsSecondary = false;
        boolean primaryReversed = false;

        public CapabilitiesItem(int capabilities, String description) {
            this.capabilities = capabilities;
            this.description = description;
        }

        /**
         * Tag to show that the 'capabilities' entry is for a shade with a secondary rail
         *
         * @return this
         */
        public CapabilitiesItem supportsSecondary() {
            supportsSecondary = true;
            return this;
        }

        /**
         * Tag to show that the 'capabilities' entry is for a primary rail with its open/closed positions reversed
         *
         * @return this
         */
        public CapabilitiesItem primaryReversed() {
            primaryReversed = true;
            return this;
        }

        /**
         * Tag to show that the 'capabilities' entry is for a shade without vanes
         *
         * @return this
         */
        public CapabilitiesItem notSupportsVane() {
            supportsVane = false;
            return this;
        }
    }

    /**
     * Determines if a given shade 'type' is in this database of known shade types
     *
     * @param type the shade 'type' parameter
     * @return true if the shade 'type' is known
     */
    public boolean isTypeInDatabase(int type) {
        for (TypeItem item : TYPE_DATABASE) {
            if ((type == item.type) && (type > 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the formatted property description text of the given shade 'type' entry
     *
     * @param type the shade 'type' parameter
     * @return formatted property description text
     */
    public String getTypeProperty(int type) {
        for (TypeItem item : TYPE_DATABASE) {
            if (type == item.type) {
                return format(item.type, item.description);
            }
        }
        return format(-1, NOT_DETERMINED);
    }

    /**
     * Determines if a given 'capabilities' value is in this database of known capabilities
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return true if the 'capabilities' value is known
     */
    public boolean isCapabilitiesInDatabase(int capabilities) {
        for (CapabilitiesItem item : CAPABILITIES_DATABASE) {
            if ((capabilities == item.capabilities) && (capabilities >= 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the formatted property description text of the given shade 'capabilities' entry
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return formatted property description text
     */
    public String getCapabilitiesProperty(int capabilities) {
        for (CapabilitiesItem item : CAPABILITIES_DATABASE) {
            if (capabilities == item.capabilities) {
                return format(item.capabilities, item.description);
            }
        }
        return format(-1, NOT_DETERMINED);
    }

    /**
     * Determine the shade 'capabilities' from its 'type'
     *
     * @param type the shade 'type' parameter
     * @return the shade 'capabilities' value
     */
    public int getTypeCapabilities(int type) {
        for (TypeItem item : TYPE_DATABASE) {
            if (type == item.type) {
                return item.capabilities;
            }
        }
        return -1;
    }

    /**
     * Determine if the shade 'capabilities' are compatible with its 'type'
     *
     * @param type the shade 'type' parameter
     * @param capabilities the shade 'capabilities' parameter
     * @return true if the 'capabilities' match the 'type'
     */
    public boolean isTypeCapabilitiesCompatibile(int type, int capabilities) {
        return (capabilities == getTypeCapabilities(type));
    }

    /**
     * Determine if the capabilities indicate support for a secondary rail
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return true if a secondary rail is supported
     */
    public boolean capabilitiesSupportsSecondary(int capabilities) {
        for (CapabilitiesItem item : CAPABILITIES_DATABASE) {
            if (capabilities == item.capabilities) {
                return item.supportsSecondary;
            }
        }
        return false;
    }

    /**
     * Determine if the capabilities indicate that the primary rail open/closed positions are reversed
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return true if the primary rail is reversed
     */
    public boolean capabilitiesPrimaryReversed(int capabilities) {
        for (CapabilitiesItem item : CAPABILITIES_DATABASE) {
            if (capabilities == item.capabilities) {
                return item.primaryReversed;
            }
        }
        return false;
    }

    private static final String PROPERTY_FORMAT_STRING = "%d ( %s )";

    /**
     * Concatenate the value and its description into a single formatted property string
     *
     * @param value
     * @param description
     * @return the concatenated formatted string
     */
    private String format(int value, String description) {
        return String.format(PROPERTY_FORMAT_STRING, value, description);
    }

    /**
     * Parse the formatted property string and return the part that represents its int value
     *
     * @param propertyString
     * @return the int value
     */
    public int getPropertyValue(String propertyString) {
        try {
            return Integer.parseInt(propertyString.substring(0, propertyString.indexOf(" ")));
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * Log a message indicating that 'type' is not in database
     *
     * @param type
     */
    public void logTypeNotInDatabase(int type) {
        logger.warn("Shade 'type:{}' is not in database => Please inform developers!", type);
    }

    /**
     * Log a message indicating that 'capabilities' is not in database
     *
     * @param capabilities
     */
    public void logCapabilitiesNotInDatabase(int type, int capabilities) {
        logger.warn("Shade 'type:{}' has 'capabilities:{}' not in database => Please inform developers!", type,
                capabilities);
    }

    /**
     * Log a message indicating 'type' and 'capabilities' are not mutually compatible
     *
     * @param type
     * @param capabilities
     */
    public void logTypeCapabilitiesNotCompatibile(int type, int capabilities) {
        logger.warn("Shade 'type:{}' and 'capabilities:{}' are not compatible in database => Please inform developers!",
                type, capabilities);
    }

    /**
     * Log a message indicating that secondary support observed via JSON payload does not match the value expected from
     * the 'type' and 'capabilities'
     *
     * @param type
     * @param capabilities
     * @param jsonSupportsSecondary
     */
    public void logSupportsSecondaryNotMatching(int type, int capabilities, boolean jsonSupportsSecondary) {
        logger.warn(
                "Shade 'jsonSupportsSecondary:{}' property does not match 'type:{}', 'capabilities:{}' in database => Please inform developers!",
                jsonSupportsSecondary, type, capabilities);
    }
}
