/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.components;

import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;

/**
 * Helper class to help translate the position values of a velux node into more
 * readable / usable references.
 *
 * @author MFK - Initial Contribution
 */
public class VeluxPosition {

    /** The Constant POSITION_OPEN. */
    public static final short POSITION_OPEN = (short) 0x0000;

    /** The Constant POSITION_CLOSED. */
    public static final short POSITION_CLOSED = (short) 0xC800;

    /** The Constant PCT_POSITION_INC. */
    public static final short PCT_POSITION_INC = (short) 0x200;

    /** The Constant POSITION_UNKNOWN. */
    protected static final int POSITION_UNKNOWN = 0xF7FF;

    /** The position. */
    private int position;

    /** Indicates that this position value refers to an unknown position */
    private boolean unknown;

    /**
     * Instantiates a new velux position.
     *
     * @param position
     *                     the position
     */
    public VeluxPosition(short position) {
        this.position = position & 0xFFFF;
        if (POSITION_UNKNOWN == this.position) {
            this.unknown = true;
        } else {
            this.unknown = false;
        }
    }

    /**
     * Indicates whether the position is known or unknown. Should be called by any client before making use of the
     * position value to first determine if the position is actually valid
     *
     * @return True if the position is unknown, False otherwise.
     */
    public boolean isUnknown() {
        return this.unknown;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (POSITION_UNKNOWN == this.position) {
            return "Unknown";
        }
        return getPercentageOpen() + " Open";
    }

    /**
     * Gets the percentage open.
     *
     * @return the percentage open
     */
    public String getPercentageOpen() {
        if (POSITION_UNKNOWN == this.position) {
            return "Unknown";
        }
        return "" + (100 - (this.position / PCT_POSITION_INC)) + "%";
    }

    /**
     * Gets the percentage closed.
     *
     * @return the percentage closed
     */
    public String getPercentageClosed() {
        if (POSITION_UNKNOWN == this.position) {
            return "Unknown";
        }
        return "" + (this.position / PCT_POSITION_INC) + "%";
    }

    /**
     * Gets the percentage closed.
     *
     * @return the percentage closed
     */
    public int getPercentageClosedAsInt() {
        if (POSITION_UNKNOWN == this.position) {
            return -1;
        }
        return (this.position / PCT_POSITION_INC);
    }

    /**
     * Sets the percent open.
     *
     * @param pct
     *                the pct
     * @return the short
     */
    public static short setPercentOpen(int pct) {
        if (pct < 0) {
            return POSITION_CLOSED;
        } else if (pct > 100) {
            return POSITION_OPEN;
        }
        return (short) (POSITION_CLOSED - ((short) (pct * PCT_POSITION_INC)));
    }

    /**
     * Sets the percent closed.
     *
     * @param pct
     *                the pct
     * @return the short
     */
    public static short setPercentClosed(int pct) {
        if (pct < 0) {
            return POSITION_OPEN;
        } else if (pct > 100) {
            return POSITION_CLOSED;
        }
        return (short) (POSITION_OPEN + ((short) (pct * PCT_POSITION_INC)));
    }

    /**
     * Gets the raw position of the velux node. Clients must interpret in the context of the type of node
     *
     * @return The raw position of the node.
     */
    public short getPosition() {
        return (short) this.position;
    }

    /**
     * Creates the.
     *
     * @param b1
     *               the b 1
     * @param b2
     *               the b 2
     * @return the velux position
     */
    public static VeluxPosition create(byte b1, byte b2) {
        return new VeluxPosition((short) KLFUtils.extractTwoBytes(b1, b2));
    }
}
