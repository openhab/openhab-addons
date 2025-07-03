/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Define the different possible meter types
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public enum MeterType {
    NOATTRIB(0, "NoAttrib"),
    BLEUE(1, "Blue"),
    JAUNE(2, "Yellow"),
    LINKY(3, "Linky"),
    PRISME(4, "Prisme"),
    OTHER(5, "Other");

    private final int id;
    private final String label;

    MeterType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public final int getId() {
        return id;
    }

    public final String getLabel() {
        return label;
    }
}
