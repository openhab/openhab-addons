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
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class representing an OBISIdentifier
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified, groupF not relevant, and groupB renamed to channel.
 */
@NonNullByDefault
public class OBISIdentifier {
    /**
     * String representing a-channel:c.d.e.f OBIS ID
     */
    private static final String OBISID_REGEX = "((\\d+)\\-)?((\\d+):)?((\\d+)\\.)(\\d+)(\\.(\\d+))?(.(\\d+))?";

    /**
     * OBIS ID pattern
     */
    private static final Pattern OBIS_ID_PATTERN = Pattern.compile(OBISID_REGEX);

    /**
     * Value to return when an invalid int was read.
     */
    private static final int INVALID_INT_READ = -1;

    /* the six individual group values of the OBIS ID */
    private final int groupA;
    private final @Nullable Integer channel;
    private final int groupC;
    private final int groupD;
    private final @Nullable Integer groupE;
    private final @Nullable Integer groupF;

    private boolean conflict;

    /**
     * Constructs a new OBIS Identifier (A-x:C.D.E.x)
     *
     * @param groupA A value
     * @param groupC C value
     * @param groupD D value
     * @param groupE E value
     */
    public OBISIdentifier(final int groupA, final int groupC, final int groupD, @Nullable final Integer groupE) {
        this(groupA, groupC, groupD, groupE, false);
    }

    /**
     * Constructs a new OBIS Identifier (A-x:C.D.E.x)
     *
     * @param groupA A value
     * @param groupC C value
     * @param groupD D value
     * @param groupE E value
     * @param conflict if true indicates this OBIS Identifier is used for different types of data.
     */
    public OBISIdentifier(final int groupA, final int groupC, final int groupD, @Nullable final Integer groupE,
            final boolean conflict) {
        this.groupA = groupA;
        this.channel = null;
        this.groupC = groupC;
        this.groupD = groupD;
        this.groupE = groupE;
        this.groupF = null;
        this.conflict = conflict;
    }

    /**
     * Creates a new {@link OBISIdentifier} of the specified String
     *
     * @param obisIDString the OBIS String ID
     * @throws ParseException if obisIDString is not a valid OBIS Identifier
     */
    public OBISIdentifier(final String obisIDString) throws ParseException {
        final Matcher m = OBIS_ID_PATTERN.matcher(obisIDString);

        if (m.matches()) {
            // Optional value A
            this.groupA = safeInt(m.group(2));

            // Optional value B
            this.channel = safeInteger(m.group(4));

            // Required value C & D
            this.groupC = safeInt(m.group(6));
            this.groupD = safeInt(m.group(7));

            // Optional value E
            this.groupE = safeInteger(m.group(9));

            // Optional value F
            this.groupF = safeInteger(m.group(11));
        } else {
            throw new ParseException("Invalid OBIS identifier:" + obisIDString, 0);
        }
    }

    private static int safeInt(final @Nullable String value) {
        try {
            return value == null ? INVALID_INT_READ : Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return INVALID_INT_READ;
        }
    }

    private static @Nullable Integer safeInteger(final @Nullable String value) {
        try {
            return value == null ? null : Integer.valueOf(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    public boolean isConflict() {
        return conflict;
    }

    /**
     * @return the groupA
     */
    public int getGroupA() {
        return groupA;
    }

    /**
     * @return the M-bus channel
     */
    public @Nullable Integer getChannel() {
        return channel;
    }

    /**
     * @return the groupC
     */
    public int getGroupC() {
        return groupC;
    }

    /**
     * @return the groupD
     */
    public int getGroupD() {
        return groupD;
    }

    /**
     * @return the groupE
     */
    public @Nullable Integer getGroupE() {
        return groupE;
    }

    /**
     * @return the groupF
     */
    public @Nullable Integer getGroupF() {
        return groupF;
    }

    @Override
    public String toString() {
        return groupA + "-" + (channel == null ? "" : (channel + ":")) + groupC + "." + groupD
                + (groupE == null ? "" : ("." + groupE)) + (groupF == null ? "" : ("*" + groupF));
    }

    /**
     * Returns whether or not both {@link OBISIdentifier} are exact equal (all identifiers match).
     *
     * If wild card matching is needed (since some fields are null in case of a wildcard) use
     * {@link #equalsWildCard(OBISIdentifier)} instead
     *
     * @return true if both OBISIdentifiers match, false otherwise
     */
    @Override
    public boolean equals(@Nullable final Object other) {
        OBISIdentifier o;
        if (other != null && other instanceof OBISIdentifier identifier) {
            o = identifier;
        } else {
            return false;
        }
        boolean result = true;

        result &= groupA == o.groupA;
        if (channel != null && o.channel != null) {
            result &= (channel.equals(o.channel));
        } else if (!(channel == null && o.channel == null)) {
            result = false;
        }
        result &= groupC == o.groupC;
        result &= groupD == o.groupD;
        if (groupE != null && o.groupE != null) {
            result &= groupE.equals(o.groupE);
        } else if (!(groupE == null && o.groupE == null)) {
            result = false;
        }
        if (groupF != null && o.groupF != null) {
            result &= (groupF.equals(o.groupF));
        } else if (!(groupF == null && o.groupF == null)) {
            result = false;
        }

        return result;
    }

    /**
     * Checks whether this OBIS Identifier and the other identifier equals taking the wildcards into account
     *
     * @param o OBISIdentifier to compare to
     *
     * @return true if identifiers match fully or against a wildcard, false otherwise
     */
    public boolean equalsWildCard(final OBISIdentifier o) {
        boolean result = true;

        result &= groupA == o.groupA;
        if (channel != null && o.channel != null) {
            result &= (channel.equals(o.channel));
        }
        result &= groupC == o.groupC;
        result &= groupD == o.groupD;
        if (groupE != null && o.groupE != null) {
            result &= (groupE.equals(o.groupE));
        }
        if (groupF != null && o.groupF != null) {
            result &= (groupF.equals(o.groupF));
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupA, (channel != null ? channel : 0), groupC, groupD, (groupE != null ? groupE : 0),
                (groupF != null ? groupF : 0));
    }

    /**
     * Returns a reduced OBIS Identifier.
     *
     * @return reduced OBIS Identifier
     */
    public OBISIdentifier getReducedOBISIdentifier() {
        return new OBISIdentifier(groupA, groupC, groupD, groupE);
    }

    /**
     * Returns a reduced OBIS Identifier with group E set to null (.i.e. not applicable)
     *
     * @return reduced OBIS Identifier
     */
    public OBISIdentifier getReducedOBISIdentifierGroupE() {
        return new OBISIdentifier(groupA, groupC, groupD, null);
    }
}
