/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal.util;

/**
 * The {@link PIdContainerClass} his a helper class for pairing IDs
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
public class PIdContainerClass {
    String valueType;
    String category;
    int min;
    int max;
    String Label;
    String Descprition;

    PIdContainerClass(String pValueType, String pCategory, String pMin, String pMax, String pLabel,
            String pDescription) {

        this.valueType = pValueType;
        this.category = pCategory;
        this.min = 0;// Integer.getInteger(pMin);
        this.max = 100;// Integer.getInteger(pMax);
        this.Label = pLabel;
        this.Descprition = pDescription;
    }

    PIdContainerClass() {
    }
}
