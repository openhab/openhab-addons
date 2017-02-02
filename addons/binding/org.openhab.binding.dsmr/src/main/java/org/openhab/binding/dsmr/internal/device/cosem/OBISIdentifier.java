/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing an OBISIdentifier
 *
 * @author M. Volaart
 * @since 2.1.0
 */
public class OBISIdentifier {
    /* String representing a.b.c.d.e.f OBIS ID */
    private static final String OBISID_REGEX = "((\\d+)\\-)?((\\d+):)?((\\d+)\\.)(\\d+)(\\.(\\d+))?(\\*(\\d+))?";

    /* OBIS ID pattern */
    private static final Pattern obisIdPattern = Pattern.compile(OBISID_REGEX);

    /* the six individual group values of the OBIS ID */
    private Integer groupA;
    private Integer groupB;
    private Integer groupC;
    private Integer groupD;
    private Integer groupE;
    private Integer groupF;

    /**
     * Constructs a new OBIS Identifier (A.B.C.D.E.F)
     *
     * @param groupA A value
     * @param groupB B value
     * @param groupC C value
     * @param groupD D value
     * @param groupE E value
     * @param groupF F value
     */
    public OBISIdentifier(Integer groupA, Integer groupB, Integer groupC, Integer groupD, Integer groupE,
            Integer groupF) {
        this.groupA = groupA;
        this.groupB = groupB;
        this.groupC = groupC;
        this.groupD = groupD;
        this.groupE = groupE;
        this.groupF = groupF;
    }

    /**
     * Creates a new OBISIdentifier of the specified String
     *
     * @param obisIDString the OBIS String ID
     * @throws ParseException if obisIDString is not a valid OBIS Identifier
     */
    public OBISIdentifier(String obisIDString) throws ParseException {
        Matcher m = obisIdPattern.matcher(obisIDString);

        if (m.matches()) {
            // Optional value A
            if (m.group(2) != null) {
                this.groupA = Integer.parseInt(m.group(2));
            }

            // Optional value B
            if (m.group(4) != null) {
                this.groupB = Integer.parseInt(m.group(4));
            }

            // Required value C & D
            this.groupC = Integer.parseInt(m.group(6));
            this.groupD = Integer.parseInt(m.group(7));

            // Optional value E
            if (m.group(9) != null) {
                this.groupE = Integer.parseInt(m.group(9));
            }

            // Optional value F
            if (m.group(11) != null) {
                this.groupF = Integer.parseInt(m.group(11));
            }
        } else {
            throw new ParseException("Invalid OBIS identifier:" + obisIDString, 0);
        }
    }

    /**
     * @return the groupA
     */
    public Integer getGroupA() {
        return groupA;
    }

    /**
     * @return the groupB
     */
    public Integer getGroupB() {
        return groupB;
    }

    /**
     * @return the groupC
     */
    public Integer getGroupC() {
        return groupC;
    }

    /**
     * @return the groupD
     */
    public Integer getGroupD() {
        return groupD;
    }

    /**
     * @return the groupE
     */
    public Integer getGroupE() {
        return groupE;
    }

    /**
     * @return the groupF
     */
    public Integer getGroupF() {
        return groupF;
    }

    @Override
    public String toString() {
        return (groupA != null ? (groupA + "-") : "") + (groupB != null ? (groupB + ":") : "") + groupC + "." + groupD
                + (groupE != null ? ("." + groupE) : "") + (groupF != null ? ("*" + groupF) : "");
    }

    /**
     * Returns whether or not both OBISIdentifier are exact equal (all identifiers match).
     *
     * If wild card matching is needed (since some fields are null in case of a wildcard) use
     * {@link #equalsWildCard(OBISIdentifier)} instead
     *
     * @return true if both OBISIdentifiers match, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        OBISIdentifier o;
        if (other != null && other instanceof OBISIdentifier) {
            o = (OBISIdentifier) other;
        } else {
            return false;
        }
        boolean result = true;

        if (groupA != null && o.groupA != null) {
            result &= (groupA.equals(o.groupA));
        } else if (!(groupA == null && o.groupA == null)) {
            result = false;
        }
        if (groupB != null && o.groupB != null) {
            result &= (groupB.equals(o.groupB));
        } else if (!(groupB == null && o.groupB == null)) {
            result = false;
        }
        result &= (groupC.equals(o.groupC));
        result &= (groupD.equals(o.groupD));
        if (groupE != null && o.groupE != null) {
            result &= (groupE.equals(o.groupE));
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
     * Checks whether this OBIS Identifer and the other identifer equals taking the wildcards into account
     *
     * @param other OBISIdentifier to compare to
     *
     * @return true if identifiers match fully or against a wildcard, false otherwise
     */
    public boolean equalsWildCard(OBISIdentifier o) {
        boolean result = true;

        if (groupA != null && o.groupA != null) {
            result &= (groupA.equals(o.groupA));
        }
        if (groupB != null && o.groupB != null) {
            result &= (groupB.equals(o.groupB));
        }
        result &= (groupC.equals(o.groupC));
        result &= (groupD.equals(o.groupD));
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
        return ((groupA != null) ? groupA.hashCode() : 0) + ((groupB != null) ? groupB.hashCode() : 0)
                + groupC.hashCode() + groupD.hashCode() + ((groupE != null) ? groupE.hashCode() : 0)
                + ((groupF != null) ? groupF.hashCode() : 0);
    }

    /**
     * Returns an reduced OBIS Identifier. This means group F is set to null
     * (.i.e. not applicable)
     *
     * @return reduced OBIS Identifer
     */
    public OBISIdentifier getReducedOBISIdentifier() {
        return new OBISIdentifier(groupA, groupB, groupC, groupD, groupE, null);
    }

    /**
     * Returns whether or not the reduced OBIS Identifier is a wildcard identifier (meaning groupA groupB or groupC is
     * null)
     * Note that the DSMR specification does not use groupF so this is implemented always as a wildcard.
     * To distuinguish wildcard from non wildcard OBISIdentifiers, groupF is ignored.
     *
     * @return true if the reducedOBISIdentifier is a wildcard identifier, false otherwise.
     */
    public boolean reducedOBISIdentifierIsWildCard() {
        return (groupA == null) || (groupB == null) || (groupC == null);
    }
}