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
package org.openhab.binding.touchwand.internal.dto;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.TYPE_UNKNOWN;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TouchWandUnknownTypeUnitData} implements unknown unit data
 * property.
 * It makes the code generic in case parsing error or unknown types
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandUnknownTypeUnitData extends TouchWandUnitData {

    public TouchWandUnknownTypeUnitData() {
        this.setType(TYPE_UNKNOWN);
    }

    @Override
    public Integer getCurrStatus() {
        return 0;
    }
}
