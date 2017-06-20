/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

/**
 * List of Errors
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum FanModeType {

    SILENT(38, "Silent"),
    STANDARD(60, "Standard"),
    POWER(77, "Power"),
    FULL(90, "Full"),
    UNKNOWN(-1, "Unknown");

    private final int id;
    private final String description;

    FanModeType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public static FanModeType getType(int value) {
        for (FanModeType st : FanModeType.values()) {
            if (st.getId() == value) {
                return st;
            }
        }
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Status " + Integer.toString(id) + " - " + description;
    }
}
