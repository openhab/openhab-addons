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
package org.openhab.binding.siemensrds.points;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.google.gson.annotations.SerializedName;

/**
 * private class a data point where "value" is a JSON text element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class StringPoint extends BasePoint {

    @SerializedName("value")
    private @Nullable String value;

    @Override
    public int asInt() {
        try {
            String value = this.value;
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // default value
        }
        return UNDEFINED_VALUE;
    }

    @Override
    public State getState() {
        return new StringType(value);
    }

    @Override
    public void refreshValueFrom(BasePoint from) {
        super.refreshValueFrom(from);
        if (from instanceof StringPoint stringPoint) {
            this.value = stringPoint.value;
        }
    }
}
