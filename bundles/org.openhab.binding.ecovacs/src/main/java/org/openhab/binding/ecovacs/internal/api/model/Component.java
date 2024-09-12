/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecovacs.internal.api.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public enum Component {
    BRUSH("Brush", "brush"),
    SIDE_BRUSH("SideBrush", "sideBrush"),
    DUST_CASE_HEAP("DustCaseHeap", "heap"),
    UNIT_CARE("" /* not supported in XML */, "unitCare");

    public final String xmlValue;
    public final String jsonValue;

    private Component(String xmlValue, String jsonValue) {
        this.xmlValue = xmlValue;
        this.jsonValue = jsonValue;
    }
}
