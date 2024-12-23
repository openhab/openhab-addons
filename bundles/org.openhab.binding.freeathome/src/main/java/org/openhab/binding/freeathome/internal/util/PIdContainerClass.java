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
package org.openhab.binding.freeathome.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PIdContainerClass} is a helper class for pairing IDs
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class PIdContainerClass {
    String valueType;
    String category;
    int min;
    int max;
    String label;
    String description;

    PIdContainerClass(String pValueType, String pCategory, String pMin, String pMax, String pLabel,
            String pDescription) {
        this.valueType = pValueType;

        this.category = pCategory;

        if (pMax.isEmpty()) {
            this.min = 0;
        } else {
            this.min = Integer.parseInt(pMin);
        }

        if (pMax.isEmpty()) {
            this.max = 100;
        } else {
            this.max = Integer.parseInt(pMax);
        }

        this.label = pLabel;

        this.description = pDescription;
    }
}
