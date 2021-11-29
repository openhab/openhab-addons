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
 * Class containing the database of all known shade 'types' and their respective 'capabilities'.
 *
 * If user systems detect shade types that are not in the database, then this class can issue logger warning messages
 * indicating such absence, and prompting the user to report it to developers so that the database and the respective
 * binding functionality can (hopefully) be extended over time.
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class ShadeCapabilitiesDatabase {

    private final Logger logger = LoggerFactory.getLogger(ShadeCapabilitiesDatabase.class);

    /*
     * Database of known shade capabilities
     */
    private static final Capabilities[] CAPABILITIES_DATABASE = {
        // @formatter:off
            new Capabilities(0).primary()       .text("Bottom Up"),
            new Capabilities(1).primary().vane().text("Bottom Up Tilt 90°"),
            new Capabilities(2).primary().vane().text("Bottom Up Tilt 180°"),
            new Capabilities(3).primary()       .text("Vertical"),
            new Capabilities(4).primary().vane().text("Vertical Tilt 180°"),
            new Capabilities(5)          .vane().text("Tilt Only 180°"),
            new Capabilities(6).primary()       .text("Top Down")                 .primaryStateInverted(),
            new Capabilities(7).primary()       .text("Top Down Bottom Up")       .secondary(),
            new Capabilities(8).primary()       .text("Duolite Lift"),
            new Capabilities(9).primary().vane().text("Duolite Lift and Tilt 90°"),
        // @formatter:on
            new Capabilities() };

    /*
     * Database of known shade types and corresponding capabilities
     */
    private static final Type[] TYPE_DATABASE = {
        // @formatter:off
            new Type( 4).capabilities(0).text("Roman"),
            new Type( 5).capabilities(0).text("Bottom Up"),
            new Type( 6).capabilities(0).text("Duette"),
            new Type( 7).capabilities(6).text("Top Down"),
            new Type( 8).capabilities(7).text("Duette Top Down Bottom Up"),
            new Type( 9).capabilities(7).text("Duette DuoLite Top Down Bottom Up"),
            new Type(23).capabilities(1).text("Silhouette"),
            new Type(42).capabilities(0).text("M25T Roller Blind"),
            new Type(43).capabilities(1).text("Facette"),
            new Type(44).capabilities(0).text("Twist"),
            new Type(47).capabilities(7).text("Pleated Top Down Bottom Up"),
            new Type(49).capabilities(0).text("AC Roller"),
            new Type(51).capabilities(2).text("Venetian"),
            new Type(54).capabilities(3).text("Vertical Slats Left Stack"),
            new Type(55).capabilities(3).text("Vertical Slats Right Stack"),
            new Type(56).capabilities(3).text("Vertical Slats Split Stack"),
            new Type(62).capabilities(2).text("Venetian"),
            new Type(69).capabilities(3).text("Curtain Left Stack"),
            new Type(70).capabilities(3).text("Curtain Right Stack"),
            new Type(71).capabilities(3).text("Curtain Split Stack"),
            new Type(79).capabilities(8).text("Duolite Lift"),
        // @formatter:on
            new Type() };

    /**
     * Base class that is extended by Type and Capabilities classes.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    public static class Base {
        protected int intValue = -1;
        protected String text = "-- not in database --";

        @Override
        public String toString() {
            return String.format("%d ( %s )", intValue, text);
        }
    }

    /**
     * Describes a shade type entry in the database; implements 'capabilities' parameter.
     *
     * @author AndrewFG - Initial contribution
     */
    public static class Type extends Base {
        private int capabilities = -1;

        protected Type() {
        }

        protected Type(int type) {
            intValue = type;
        }

        protected Type text(String text) {
            this.text = text;
            return this;
        }

        protected Type capabilities(int capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        /**
         * Get shade types's 'capabilities'.
         *
         * @return 'capabilities'.
         */
        public int getCapabilities() {
            return capabilities;
        }
    }

    /**
     * Describes a shade 'capabilities' entry in the database; adds properties indicating its supported functionality.
     *
     * @author AndrewFG - Initial contribution
     */
    public static class Capabilities extends Base {
        private boolean supportsPrimary;
        private boolean supportsVanes;
        private boolean supportsSecondary;
        private boolean primaryStateInverted;

        protected Capabilities() {
        }

        protected Capabilities(int capabilities) {
            intValue = capabilities;
        }

        protected Capabilities text(String text) {
            this.text = text;
            return this;
        }

        protected Capabilities primary() {
            supportsPrimary = true;
            return this;
        }

        protected Capabilities vane() {
            supportsVanes = true;
            return this;
        }

        protected Capabilities secondary() {
            supportsSecondary = true;
            return this;
        }

        protected Capabilities primaryStateInverted() {
            primaryStateInverted = true;
            return this;
        }

        /**
         * Check if the Capabilities class instance supports a primary shade.
         *
         * @return true if it supports a primary shade.
         */
        public boolean supportsPrimary() {
            return supportsPrimary;
        }

        /**
         * Check if the Capabilities class instance supports a vane.
         *
         * @return true if it supports a vane.
         */
        public boolean supportsVanes() {
            return supportsVanes;
        }

        /**
         * Check if the Capabilities class instance supports a secondary shade.
         *
         * @return true if it supports a secondary shade.
         */
        public boolean supportsSecondary() {
            return supportsSecondary;
        }

        /**
         * Check if the Capabilities class instance supports a secondary shade.
         *
         * @return true if the primary shade is inverted.
         */
        public boolean isPrimaryStateInverted() {
            return primaryStateInverted;
        }
    }

    /**
     * Determines if a given shade 'type' is in the database.
     *
     * @param type the shade 'type' parameter.
     * @return true if the shade 'type' is known.
     */
    public boolean isTypeInDatabase(int type) {
        for (Type item : TYPE_DATABASE) {
            if ((type == item.intValue) && (type > 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a given 'capabilities' value is in the database.
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return true if the 'capabilities' value is known
     */
    public boolean isCapabilitiesInDatabase(int capabilities) {
        for (Capabilities item : CAPABILITIES_DATABASE) {
            if ((capabilities == item.intValue) && (capabilities >= 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return a Type class instance that corresponds to the given 'type' parameter.
     *
     * @param type the shade 'type' parameter.
     * @return corresponding instance of Type class.
     */
    public Type getType(int type) {
        for (Type item : TYPE_DATABASE) {
            if (type == item.intValue) {
                return item;
            }
        }
        return new Type();
    }

    /**
     * Return a Capabilities class instance that corresponds to the given 'capabilities' parameter.
     *
     * @param capabilities the shade 'capabilities' parameter.
     * @return corresponding instance of Capabilities class.
     */
    public Capabilities getCapabilities(int capabilities) {
        for (Capabilities item : CAPABILITIES_DATABASE) {
            if (capabilities == item.intValue) {
                return item;
            }
        }
        return new Capabilities();
    }

    private static final String REQUEST_DEVELOPERS_TO_UPDATE = " => Please request developers to update the database!";

    /**
     * Log a message indicating that 'type' is not in database.
     *
     * @param type
     */
    public void logTypeNotInDatabase(int type) {
        logger.warn("The shade 'type:{}' is not in the database!{}", type, REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating that 'capabilities' is not in database.
     *
     * @param capabilities
     */
    public void logCapabilitiesNotInDatabase(int type, int capabilities) {
        logger.warn("The 'capabilities:{}' for shade 'type:{}' are not in the database!{}", capabilities, type,
                REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating the type's capabilities and the passed capabilities are not equal.
     *
     * @param type
     * @param capabilities
     */
    public void logCapabilitiesMismatch(int type, int capabilities) {
        logger.warn("The 'capabilities:{}' reported by shade 'type:{}' don't match the database!{}", capabilities, type,
                REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating that a shade's secondary/vanes support, as observed via its actual JSON pay-load, does
     * not match the expected value as declared in its 'type' and 'capabilities'.
     *
     * @param propertyKey
     * @param type
     * @param capabilities
     * @param propertyValue
     */
    public void logPropertyMismatch(String propertyKey, int type, int capabilities, boolean propertyValue) {
        logger.warn(
                "The '{}:{}' property actually reported by shade 'type:{}' is different "
                        + "than expected from its 'capabilities:{}' in the database!{}",
                propertyKey, propertyValue, type, capabilities, REQUEST_DEVELOPERS_TO_UPDATE);
    }
}
