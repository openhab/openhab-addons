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
package org.openhab.binding.hdpowerview.internal.api._v3;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.BatteryKind;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;

/**
 * State of a Shade as returned by an HD PowerView hub of Generation 3.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataV3 extends ShadeData {
    public @Nullable String ptName;
    public @Nullable String powerType;
    public @Nullable String bleName;
    // TODO: public @Nullable Motion motion;

    @Override
    public String getName() {
        return String.join(" ", super.getName(), ptName);
    }

    @Override
    public BatteryKind getBatteryKind() {
        // TODO Auto-generated method stub
        // NOTE: the schema for powerType is not clear; is may be a string? or an integer?
        return BatteryKind.ERROR_UNKNOWN;
    }

    @Override
    public int version() {
        return 3;
    }
}
